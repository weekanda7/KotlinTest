# Android View ID 命名規範（Espresso 用）

> 適用：Kotlin + XML View + Material Components，測試使用 Espresso。
> 來源：由過去 Web 專案的 testid spec（React + Ant Design）改寫，改採 Android 社群慣例（元件類型前綴）。

---

## 1. 命名總則

| 項目 | 規則 |
|---|---|
| 格式 | `{元件類型}_{主體}[_{補充}]`，例：`button_login`、`edit_email`、`text_error_password` |
| 大小寫 | 全小寫 snake_case；**不可**用 `-`、大寫、數字開頭（Android resource 限制） |
| 類型前綴 | 使用完整單字，不縮寫：`text_` 而非 `tv_`，`button_` 而非 `btn_` |
| 主體 | 取自畫面上的英文標題，轉成 snake_case：`Email address` → `email_address` |
| 畫面前綴 | **不加**。每個 layout 是獨立的查找範圍，同名 id 靠容器區分（見第 5 節） |
| ViewBinding | id 會自動轉成 camelCase：`button_login` → `binding.buttonLogin` |

---

## 2. 前綴字典

| 元件 | 前綴 | 範例 | ViewBinding |
|---|---|---|---|
| `TextView` | `text_` | `text_title`、`text_email` | `textTitle` |
| `EditText` / `TextInputEditText` | `edit_` | `edit_password` | `editPassword` |
| `TextInputLayout` | `input_layout_` | `input_layout_password` | `inputLayoutPassword` |
| `Button` / `MaterialButton` | `button_` | `button_login` | `buttonLogin` |
| `ImageButton`（純 icon） | `button_` | `button_share` | `buttonShare` |
| `FloatingActionButton` | `fab_` | `fab_add_device` | `fabAddDevice` |
| `ImageView` | `image_` | `image_logo` | `imageLogo` |
| `CheckBox` | `checkbox_` | `checkbox_remember_me` | `checkboxRememberMe` |
| `RadioGroup` / `RadioButton` | `radio_group_` / `radio_` | `radio_group_os`、`radio_linux` | `radioLinux` |
| `MaterialSwitch` | `switch_` | `switch_notifications` | `switchNotifications` |
| 下拉選單（`MaterialAutoCompleteTextView`） | `dropdown_` | `dropdown_language` | `dropdownLanguage` |
| `RecyclerView` | `recycler_`（複數） | `recycler_devices` | `recyclerDevices` |
| `MaterialCardView` | `card_` | `card_device_status` | `cardDeviceStatus` |
| `ChipGroup` / `Chip` | `chip_group_` / `chip_` | `chip_group_tags` | `chipGroupTags` |
| `TabLayout` / `ViewPager2` | `tab_layout` / `pager` | `tab_layout`、`pager` | `tabLayout` |
| `ProgressBar` | `progress_` | `progress_loading` | `progressLoading` |
| 容器 layout（有測試需要才加） | `layout_` | `layout_empty_state` | `layoutEmptyState` |
| `MaterialToolbar` | `toolbar` | `toolbar` | `toolbar` |
| `BottomNavigationView` | `bottom_nav` | `bottom_nav` | `bottomNav` |
| Menu XML 的動作項目 | `action_` | `action_edit`、`action_delete` | — |
| Bottom nav 的項目 | `navigation_` | `navigation_dashboard` | — |

---

## 3. 表單欄位

Material 的輸入欄位由兩層組成，label 和錯誤訊息都在外層：

```
input_layout_{name}   → TextInputLayout（label、錯誤訊息、密碼眼睛圖示）
edit_{name}           → TextInputEditText（實際輸入）
text_{name}           → 唯讀頁面顯示值
```

| 情境 | 定位方式 |
|---|---|
| 輸入文字 | `withId(R.id.edit_email)` |
| 驗證欄位錯誤訊息 | 在 `input_layout_email` 上比對 error（需自訂 matcher），或 `withText(R.string.error_email_required)` |
| 下拉選單選項 | 選項在 popup 裡、沒有 id：`onView(withText("English")).inRoot(isPlatformPopup())` |

---

## 4. 定位優先順序

| 優先 | 方式 | 適用 |
|---|---|---|
| 1 | `withId(R.id.xxx)` | 所有需要操作或驗證的元素 |
| 2 | `withText(R.string.xxx)` | Dialog 按鈕、Snackbar、Toast、下拉選項、動態產生的 Chip |
| 3 | `withContentDescription(...)` | 純 icon、狀態圖示、Toolbar 返回鍵 |
| 4 | `withHint(...)` | 沒有 `TextInputLayout` 的單純輸入框 |
| 5 | 不定位 | 純裝飾元素不需要 id |

文字定位一律用 `R.string`，**不要寫死字串**，避免多語系時測試失敗。

---

## 5. 範圍限定

Espresso 的 `withId` 在目前畫面上找到多個相同 id 時，會丟出 `AmbiguousViewMatcherException`。以下情況必須先定位容器：

**RecyclerView 的每一列**（`item_device.xml` 裡的 `text_name` 每列都有）：

```kotlin
onView(withId(R.id.recycler_devices)).perform(
    RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
        hasDescendant(withText("server-01")), click()
    )
)
```

**同一個 layout 被 `<include>` 多次**：

```kotlin
onView(allOf(withId(R.id.text_title), isDescendantOfA(withId(R.id.card_device_status))))
```

**Dialog 內的元素**：

```kotlin
onView(withText(R.string.action_confirm)).inRoot(isDialog()).perform(click())
```

---

## 6. 從 Ant Design 規則的對照

| 原 spec | 本規範 |
|---|---|
| `email-input` / `email-field` / `email` | `edit_email` / `input_layout_email` / `text_email` |
| `language-select` / `language-option` | `dropdown_language` / 選項用 `withText` + `isPlatformPopup()` |
| `option-edit`（More 選單） | menu XML 的 `action_edit` |
| `tc-title-*` / `tc-*`（表格） | `recycler_*` + item layout 內的 `text_*`，不需 `tc_` 前綴 |
| Modal class contains | `inRoot(isDialog())` + `withText` |
| `meta-*-area`（卡片） | `card_*` |
| `tag` / `tag-more` | `chip_group_tags`，chip 用文字定位 |
| `status-running` 等燈號 | `image_status` + `contentDescription` |
| `page-title`、`back-icon-button` | `toolbar` 內用 `withText`；返回鍵用 contentDescription |
| `*-menu-item` | `navigation_*` |
| 紅色錯誤訊息用文字驗證 | 不變，改用 `withText(R.string.error_*)` |
| Tooltip | 不列入規範 |

---

## 7. 經驗與維護重點

- **一個畫面一份 id 清單**，備註欄標記「待實作 / 待修改 / 版本變更」，作為 QA 與開發的共同依據。
- **用 lint 擋格式錯誤**：原專案出現過屬性名稱混用和拼字錯誤（`tc-tiitle`），靠人工檢查會漏。
- **通用名稱容易撞名**：`text_title`、`text_name` 這類 id 只在有容器限定時使用。
- **把 id 列入 PR checklist**，避免功能做完才發現缺漏。
- **UI 框架無法加 id 的元素**（popup 選項、動態 chip），在清單中註明替代定位方式。

---

## 8. 範例：登入畫面

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/input_layout_username"
    android:hint="@string/label_username">
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/edit_username" />
</com.google.android.material.textfield.TextInputLayout>

<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/input_layout_password"
    android:hint="@string/label_password"
    app:endIconMode="password_toggle">
    <com.google.android.material.textfield.TextInputEditText
        android:id="@+id/edit_password"
        android:inputType="textPassword" />
</com.google.android.material.textfield.TextInputLayout>

<CheckBox android:id="@+id/checkbox_remember_me" />
<Button   android:id="@+id/button_login" />
<TextView android:id="@+id/text_forgot_password" />
```

（省略 `layout_width` 等排版屬性）

```kotlin
@Test
fun login_withEmptyPassword_showsError() {
    onView(withId(R.id.edit_username)).perform(typeText("henry"), closeSoftKeyboard())
    onView(withId(R.id.button_login)).perform(click())
    onView(withText(R.string.error_password_required)).check(matches(isDisplayed()))
}
```
