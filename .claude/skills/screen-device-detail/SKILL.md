---
name: screen-device-detail
description: Reference for the Device Detail screen (DeviceDetailActivity / activity_device_detail.xml) - single device view and delete confirmation flow. Use when modifying detail fields, the delete dialog, or the result passed back to the device list.
---

# Device Detail Screen

**Files**: `DeviceDetailActivity.kt`, `res/layout/activity_device_detail.xml`, `DeviceDetailActivityTest.kt`, `DeleteDeviceFlowTest.kt`

## Purpose

Read-only view of a single `Device` from `DeviceCatalog`, with a delete action. Reached by tapping a row in [[screen-device-list-tab]].

## Entry

Started via `deviceDetailLauncher.launch(...)` from `DeviceListFragment` with `EXTRA_DEVICE_ID` (String). `onCreate()` looks the device up with `DeviceCatalog.findById(id)`; if not found (e.g. stale id), the activity calls `finish()` immediately with no result and no UI is shown.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `toolbar` | `toolbar` | `MaterialToolbar` | Title = device name; back arrow calls `finish()` |
| `text_name` | `textName` | `TextView` | Device name |
| `text_status` | `textStatus` | `TextView` | `R.string.text_status_online`/`offline` |
| `text_ip_address` | `textIpAddress` | `TextView` | Device IP |
| `text_type` | `textType` | `TextView` | `R.string.text_type` formatted with device type |
| `button_delete` | `buttonDelete` | `MaterialButton` (outlined, error color) | Opens the delete confirmation dialog |

## Behavior

- `confirmDelete()`: `AlertDialog` (`dialog_delete_title`/`dialog_delete_message`, positive = `dialog_delete_positive` → `deleteDevice()`, negative = `dialog_delete_negative` → dismiss only).
- `deleteDevice()`: `DeviceCatalog.remove(device.id)`, then `setResult(RESULT_OK, Intent().putExtra(EXTRA_DELETED, true))`, then `finish()`. The caller (`DeviceListFragment`) reads `EXTRA_DELETED` to decide whether to refresh and show the "device deleted" Snackbar.

## Window insets & back-arrow gotcha

Root view `device_detail` has `ViewCompat.setOnApplyWindowInsetsListener` padding for `systemBars()`, and `app:navigationIcon` is `@drawable/ic_arrow_back` (not `?attr/homeAsUpIndicator`, which resolves to nothing under this app's `NoActionBar` theme). This was the screen where the "back arrow does nothing" bug was first diagnosed — see [[edge-to-edge-insets]] for the full root cause, since the same fix had to be repeated on [[screen-device-list-shell]] and [[screen-add-device]] too.

## Navigation

Device Detail → `finish()` back to the Devices tab, either plain (back arrow) or with `RESULT_OK` + `EXTRA_DELETED=true` (after a successful delete).
