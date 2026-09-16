---
name: screen-device-list-tab
description: Reference for the Devices tab content (DeviceListFragment / fragment_device_list.xml + item_device.xml) - search, list, empty/loading state, and the add/detail navigation results. Use when modifying the device list, search filtering, or how added/deleted devices refresh the list. Hosted inside [[screen-device-list-shell]].
---

# Devices Tab (DeviceListFragment)

**Files**: `DeviceListFragment.kt`, `res/layout/fragment_device_list.xml`, `res/layout/item_device.xml`, `DeviceAdapter.kt`, `DeviceRepository.kt`, `Device.kt` (`DeviceCatalog`), `DeviceSearchTest.kt`, `AddDeviceFlowTest.kt`, `DeleteDeviceFlowTest.kt`

## Purpose

Default tab of `DeviceListActivity`. Shows the device list with search, and is the jump-off point to add or view/delete a device.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `search_devices` | `searchDevices` | `SearchView` | Disabled until the initial load finishes |
| `recycler_devices` | `recyclerDevices` | `RecyclerView` | `LinearLayoutManager`, adapter = `DeviceAdapter` |
| `text_empty_state` | `textEmptyState` | `TextView` | Shown when the filtered list is empty |
| `progress_loading` | `progressLoading` | `ProgressBar` | Shown only during the initial `loadDevices()` |
| `fab_add_device` | `fabAddDevice` | `FloatingActionButton` | Launches `AddDeviceActivity` |

Row layout (`item_device.xml`, inside `MaterialCardView` `card_device`): `image_status` (12dp dot, tinted by `R.color.device_status_online`/`offline`), `text_name`, `text_ip_address`. Whole card is clickable.

## Data source

`DeviceCatalog` (in `Device.kt`) is an in-memory, process-wide singleton — **not** a real database. `DeviceRepository.loadDevices()` simulates a network call: 1200ms delay on a background executor, result posted to main thread, wrapped with `EspressoIdlingResource` so Espresso tests can wait for it without a manual sleep. Because `DeviceCatalog` is a singleton, instrumented tests that add/delete devices must call `DeviceCatalog.reset()` in `@Before` (see `ResetDeviceCatalogRule.kt`) or state leaks across tests in the same instrumentation process.

## Behavior

- `onViewCreated()`: disables search, wires `DeviceAdapter` (click → detail), wires FAB (→ add), then calls `loadDevices()`.
- `loadDevices()`: shows the progress bar, calls `DeviceRepository.loadDevices { ... }`; on result, stores `allDevices`, submits to the adapter, hides progress bar, re-enables search. Guards against a fragment view that was destroyed mid-flight (`if (_binding == null) return@loadDevices`).
- `filterDevices(query)`: client-side `contains(query, ignoreCase = true)` on `device.name`; toggles `text_empty_state` vs `recycler_devices` visibility based on the filtered result.
- Row click → `Intent(DeviceDetailActivity::class.java)` with `EXTRA_DEVICE_ID`, launched via `deviceDetailLauncher` (`registerForActivityResult`). On `RESULT_OK` + `EXTRA_DELETED == true`: `refreshList()` (re-reads `DeviceCatalog.all`, re-applies the current query) + a `text_device_deleted` Snackbar.
- FAB click → `Intent(AddDeviceActivity::class.java)`, launched via `addDeviceLauncher`. On `RESULT_OK` + `EXTRA_ADDED == true`: same `refreshList()` + a `text_device_added` Snackbar.

## Navigation

Devices tab → `DeviceDetailActivity` (row tap, expects a delete result back) or `AddDeviceActivity` (FAB, expects an add result back). Both use `startActivityForResult`-style contracts, not plain `startActivity`, specifically so this fragment can refresh without a full reload.
