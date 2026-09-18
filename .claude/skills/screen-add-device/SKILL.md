---
name: screen-add-device
description: Reference for the Add Device screen (AddDeviceActivity / activity_add_device.xml) - device creation form, type dropdown, and date picker. Use when modifying the add-device form fields, validation, or the result passed back to the device list.
---

# Add Device Screen

**Files**: `AddDeviceActivity.kt`, `res/layout/activity_add_device.xml`, `AddDeviceFlowTest.kt`

## Purpose

Form to create a new `Device` and add it to `DeviceCatalog`. Reached from the FAB on [[screen-device-list-tab]].

## Entry

Started via `addDeviceLauncher.launch(...)` from `DeviceListFragment`, no extras.

## Key views

| ID | ViewBinding | Type | Notes |
|---|---|---|---|
| `toolbar` | `toolbar` | `MaterialToolbar` | Back arrow calls `finish()` |
| `input_layout_name` / `edit_name` | `inputLayoutName` / `editName` | `TextInputLayout` / `TextInputEditText` | Required |
| `input_layout_ip_address` / `edit_ip_address` | `inputLayoutIpAddress` / `editIpAddress` | `TextInputLayout` / `TextInputEditText` | Required, **no format validation** — any non-blank string passes |
| `input_layout_type` / `dropdown_type` | `inputLayoutType` / `dropdownType` | `TextInputLayout.ExposedDropdownMenu` / `MaterialAutoCompleteTextView` | Options = `DeviceCatalog.types` (`Server`, `Router`, `Printer`, `NAS`, `Other`); defaults to the first entry |
| `input_layout_install_date` / `edit_install_date` | `inputLayoutInstallDate` / `editInstallDate` | `TextInputLayout` / `TextInputEditText` | `focusable="false"`, `clickable="true"` — tapping opens a `DatePickerDialog` instead of the keyboard; optional field |
| `checkbox_online` | `checkboxOnline` | `CheckBox` | Defaults to checked (`android:checked="true"`) |
| `button_save` | `buttonSave` | `MaterialButton` | Triggers `attemptSave()` |

## Behavior

- `showDatePicker()`: opens a `DatePickerDialog` seeded with `Calendar.getInstance()`; on pick, formats as `yyyy-MM-dd` into the `installDate` field and mirrors it into `edit_install_date`'s text.
- `attemptSave()`:
  - `name` and `ipAddress` are trimmed and must be non-blank, else `input_layout_name`/`input_layout_ip_address` show `error_name_required`/`error_ip_required` and save is aborted.
  - No IP format check, no duplicate-name check.
  - Builds a `Device(id = DeviceCatalog.nextId(), name, ipAddress, isOnline = checkbox_online.isChecked, type = dropdown_type.text, installDate)` and calls `DeviceCatalog.add(device)`.
  - `DeviceCatalog.nextId()` = `(max existing numeric id) + 1` as a string — if ids are ever non-numeric this silently falls back to id `"1"`-based counting from 0.
  - `setResult(RESULT_OK, Intent().putExtra(EXTRA_ADDED, true))`, then `finish()`.

## Window insets & back-arrow gotcha

Root view `add_device` calls `applySystemBarInsetsPadding()` for the `systemBars()` insets, and `app:navigationIcon` is `@drawable/ic_arrow_back` (not `?attr/homeAsUpIndicator`) — same fix as [[screen-device-detail]] and [[screen-device-list-shell]], see [[edge-to-edge-insets]].

## Navigation

Add Device → `finish()` back to the Devices tab, either plain (back arrow, no result) or with `RESULT_OK` + `EXTRA_ADDED=true` (after a successful save).
