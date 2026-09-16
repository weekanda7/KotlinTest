# Espresso 測試踩坑紀錄

> 適用：本專案（Kotlin + XML View + Material Components，Espresso + `connectedAndroidTest`）。
> 每一項都是這次開發登入 → 裝置清單/搜尋/新增/刪除 → 設定/權限流程時，在 `Pixel_8(AVD)` 上實測遇到的失敗，附實際錯誤訊息與修法，不是憑空列的檢查表。

---

## 1. AppCompat / Material 元件的自訂屬性要用 `app:` namespace，不是 `android:`

| 元件 | 錯誤寫法 | 正確寫法 | 症狀 |
|---|---|---|---|
| `MaterialToolbar` | `android:title="@string/x"` | `app:title="@string/x"` | 標題 `TextView` 根本沒被建立（Toolbar `child-count` 少一個），`onView(withText(...))` 直接 `NoMatchingViewException` |
| `SearchView` | `android:iconifiedByDefault="false"` | `app:iconifiedByDefault="false"` | `SearchView` 永遠停留在收合狀態，內部 `search_src_text` 量測後 `width=0, height=0`，`typeText()` 丟 `PerformException`（"does not match ... getGlobalVisibleRect() to return non-empty rectangle"） |

**規則**：這類寫錯 namespace 的屬性**編譯不會報錯、執行也不會 crash**，只有在你真的去戳那個功能、或寫 Espresso 斷言時才會發現。原因是 AppCompat/Material 的自訂 View（`Toolbar`、`SearchView`…）為了向下相容，把 title/queryHint/iconifiedByDefault 這類屬性定義成自己的 `styleable`，不是承接 platform View 的同名 `android:` 屬性。加任何 Material widget 的非標準屬性（不是 `layout_width`/`text`/`padding` 這種通用屬性）時，先假設它是 `app:` namespace，寫完後至少跑一次真機/模擬器確認外觀，光看 `assembleDebug` 成功不代表屬性真的生效。

---

## 2. 「可展開/收合的 `SearchView` 選單項目」在測試上很不穩定，直接放常駐的比較可靠

一開始把 `SearchView` 設成 `app:showAsAction="always|collapseActionView"` 的選單項目，點擊 toolbar 圖示才展開。即使修對了 `iconifiedByDefault`，這個「選單項目展開/收合」機制在部分情境下仍然不可靠（`click()` 後 view hierarchy 顯示選單項目還是收合圖示，沒有真的換成 `SearchView`）。

**解法**：不用選單機制，把 `SearchView` 直接當一般 View 放在 layout 裡、`app:iconifiedByDefault="false"` 讓它常駐展開。少了「點擊展開」這個步驟，少了一整類跟 `ActionMenuPresenter`/`collapseActionView` 相關的不穩定因素，測試也更短。真的需要收合式搜尋列（節省畫面空間）時才考慮選單項目寫法，並預期要多花時間排除這類 timing 問題。

---

## 3. `typeText()` 在模擬器 IME 上會不穩定漏字，需要精確內容時改用 `replaceText()`

實際觀察到的漏字（同一支測試、不同次執行）：

| 打算輸入 | 實際存到的值 |
|---|---|
| `"test-device"` | `"test-"` |
| `"testdevice"` | `"testd"` |
| `"router"`（搜尋框） | `"r"` |

**根因**：`typeText()` 是透過 `Instrumentation.sendStringSync()` 模擬按鍵，跟 emulator 軟體鍵盤（Gboard）的 composing/commit 時機互相競爭，字元在某個時間點被截斷 — 不是 app 邏輯錯誤，重跑同一支測試漏字位置還會不一樣。

**解法**：不是刻意要測「鍵盤輸入行為」本身時（例如只是要確保欄位存到正確內容），一律用 `replaceText()`，它直接呼叫 `EditText.setText()`，完全繞過 IME。`SearchView` 的 `OnQueryTextListener` 一樣會被 `setText()` 觸發，不影響過濾邏輯。

**排查方法論**：症狀是「UI 斷言（`hasDescendant`/RecyclerView 筆數）跟預期不符」，很容易誤判成「RecyclerView 沒刷新」或「資料沒存到」。與其在 UI 層瞎猜，先加一個**跳過 UI、直接讀資料層**的 `assertEquals`（例如 `DeviceCatalog.all.last().name`）。資料層就已經是錯的 → 輸入/資料問題；資料層是對的但 UI 找不到 → 才是排版/timing 問題。這次就是靠這個二分法，兩輪之內從「猜是 RecyclerView 排版問題」收斂到「其實是 `typeText()` 漏字」。

---

## 4. 背景執行緒的非同步工作，Espresso 預設看不到 — 要用 `IdlingResource`

Espresso 會自動同步 UI thread 的 `Looper` 訊息佇列和動畫，但**看不到背景執行緒 + `Thread.sleep()`** 這類工作（模擬網路請求正是這個形狀：背景執行緒延遲後把結果 post 回主執行緒的 callback）。

- 沒註冊 `IdlingResource` 就去戳 UI：會依裝置速度隨機出現 `NoMatchingViewException`，快的裝置可能剛好蒙混過去，CI 上機器變慢就開始跳。
- 解法：用 `CountingIdlingResource`（`androidx.test.espresso:espresso-idling-resource`，**要放在 `implementation`，不是 `androidTestImplementation`** — 因為要在 production code 裡呼叫 increment/decrement），背景工作開始前 `increment()`、拿到結果後 `decrement()`；測試端寫一個 `TestWatcher`，在 `starting()`/`finished()` 呼叫 `IdlingRegistry.getInstance().register/unregister`，用 `@get:Rule(order = 0)`（比 `ActivityScenarioRule` 更外層）確保 Activity 啟動前就註冊好。

---

## 5. process-wide 的可變單例會讓測試互相汙染

沒有設定 AndroidTestOrchestrator 的話，一個測試類別（甚至整個測試 run）都跑在**同一個 app process**，`object` 單例（例如存裝置清單的 `DeviceCatalog`）的狀態會在測試方法之間、測試類別之間殘留。

新增/刪除裝置的測試會直接改到 `DeviceCatalog` 的內容；如果沒有重置，後面假設「預設有 5 筆假資料」的測試會莫名其妙壞掉，而且**壞掉的原因跟你剛動的程式碼完全無關**，很容易讓人抓錯方向。

**解法**：寫一個 `ResetDeviceCatalogRule`（`TestWatcher`），在 `starting()` 呼叫 `DeviceCatalog.reset()`，一樣用 `@get:Rule(order = 0)` 確保在 `ActivityScenarioRule` 啟動 Activity（進而讀取 `DeviceCatalog`）**之前**執行。任何會被測試修改的 process-wide 可變狀態，都要有對應的 reset rule，並放在 rule chain最外層。

---

## 6. 執行時權限（Runtime Permission）測試用 `GrantPermissionRule`，不要真的去操作系統對話框

系統權限對話框（例如 `POST_NOTIFICATIONS`）活在 app process 之外，Espresso 完全碰不到，只能靠 `UiAutomator` 操作，維護成本高、速度慢、又容易因系統 UI 版本差異而壞掉。

**解法**：用 `androidx.test:rules` 的 `GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)`，在測試開始前直接授權，讓程式碼走「已授權」這條確定性路徑。業界大多數團隊也是這樣測 — 「使用者已授權後功能正常」用 `GrantPermissionRule`；「使用者拒絕授權時的處理」通常改用單元測試直接呼叫 callback（`registerForActivityResult` 的 lambda）驗證，不靠 UI 測試碰真的系統對話框。

---

## 7. RecyclerView 用 `notifyDataSetChanged()` 增加筆數後，斷言前先 `scrollToPosition`

新增一筆資料、`adapter.submitList()` 觸發 `notifyDataSetChanged()` 之後，新插入的那個 position 有可能還沒排版完成就被 Espresso 判定為「idle」。保險做法：斷言前先做一次 `RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position)`，強制 RecyclerView 把該位置排版完成，再檢查內容。這次沒有實際證實是這個問題本身造成失敗（後來發現是第 3 點的 `typeText()` 漏字），但保留這個防禦性寫法無害，且是 RecyclerView + Espresso 常見的建議做法。

---

## 8. 除錯方法論小結

這次排查全程都是靠貼 `./gradlew connectedAndroidTest` 的完整輸出，不是憑感覺猜：

1. 先用 `TEST-<device>.xml`（`app/build/outputs/androidTest-results/connected/debug/`）抓哪些 `<testcase>` 有 `<failure>` 子節點，一次看全貌而不是只看第一個失敗。
2. 看失敗訊息裡的 **view hierarchy dump**（`child-count`、`width`/`height`、`text=`），而不是只看最上面那行 exception 訊息 — 這次兩個「明明程式碼看起來對」的 bug（`app:title`、`app:iconifiedByDefault`）都是從 dump 裡「這個 View 的 child-count 不對」「這個 View 量測後是 0×0」反推出來的。
3. UI 斷言失敗、但懷疑可能是資料錯不是排版錯的時候，加一個跳過 UI、直接讀 data layer 的斷言（`assertEquals`）先二分排查，避免在 UI 層瞎猜。
4. 每次只出一個假設、改一處、重新整包（`assembleDebug` + `assembleDebugAndroidTest`）確認能編譯，再請人在有 `adb`/模擬器的機器上重新整套跑，不要一次改多個不相關的地方，否則下次失敗不知道是哪個改動造成的。
