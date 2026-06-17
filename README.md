# Daily Mood 考拉日記 🐨

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Gemini AI](https://img.shields.io/badge/AI-Gemini%202.5%20Flash-F9AB00?style=flat-square&logo=googlegemini&logoColor=white)

> [cite_start]**「Daily Mood」不只是一個課程專案，更是一個具備溫度的情緒陪伴工具。** [cite: 207]
> [cite_start]我們以慢活、療癒的「無尾熊」作為核心，將 App 定義為一個溫溫柔柔的樹洞，讓你的每一份心情都能得到即時的溫暖回應 [cite: 218]。

---

## 📌 專案製作動機

[cite_start]在課業、人際、家庭或情感的多重壓力下，現代人的生活節奏往往快得讓人沒有時間檢視自己一整天的心情，堆積已久的負面感受容易導致心理健康問題 [cite: 217]。

[cite_start]市面上的心情日記大多像冷冰冰的記帳本，純粹紀錄缺乏動力 [cite: 218][cite_start]。因此，我們開發了「考拉日記」，提供一個溫暖、無壓力的互動空間 [cite: 217][cite_start]。專案結合了 **Google Gemini AI 技術**，模擬慵懶、溫柔的無尾熊語調進行即時情感回饋，並提供豐富的數據視覺化圖表，提升使用者對自我心理狀態的認知 [cite: 218]。

---

## 🚀 核心功能介紹

### 1. 🐨 首頁互動與沉浸式體驗
* [cite_start]**動態無尾熊：** 點擊首頁無尾熊會觸發可愛的搖晃與揮手動畫，並伴隨打招呼的文字與語音 [cite: 65, 208]。
* [cite_start]**五感沉浸環境：** 全介面按鈕（除無尾熊外）均點擊內建特製「氣泡音」音效，並搭配舒適的背景音樂 (BGM) 與動態物理基礎果凍動畫 [cite: 65, 203]。
* [cite_start]**定時推送通知：** 每天固定時間（21:00）貼心提醒，邀請你停下腳步記錄今天的生活 [cite: 200, 207]。

### 2. ✍️ 直覺式心情日記與 Gemini AI 智慧回饋
* [cite_start]**直覺心情選擇：** 提供 5 種專屬無尾熊表情按鈕（開心、難過、生氣、興奮、平靜）[cite: 88, 235]。
* [cite_start]**Gemini 2.5-flash 情感分析：** 儲存日記時，AI 會化身為充滿同理心的無尾熊朋友，根據日記文字與心情，在 60 字內生成一段溫柔、文學感且專屬的療癒小語 [cite: 88, 203]。

### 3. 📅 互動式日曆檢視介面
* [cite_start]**心情色點標記：** 日曆會自動掃描當月資料，在寫過日記的日期下方繪製對應心情顏色的圓點（如粉紅代表開心、深紅代表生氣），一目了然整月情緒 [cite: 243]。
* [cite_start]**互動詳情卡片：** 點擊過去日期會透過平滑動畫 (`SmoothEntranceAnim`) 滑出當天日記詳情並支援內容修改 [cite: 4, 245]。
* [cite_start]**防呆機制：** 針對「未來」的日期會鎖定修改按鈕，防止錯誤操作 [cite: 4, 246]。

### 4. 📊 進階 Canvas 數據統計分析
* [cite_start]**雙重維度分析：** 支援透過分段控制項 (`Segmented Control`) 切換「本週趨勢」與「本月分佈」 [cite: 281]。
* [cite_start]**五維心情雷達圖 (`MoodRadarChart`)：** 捨棄傳統折線圖，利用自定義 Canvas 繪製雷達圖，將五種情緒量化，直觀反映情緒傾向 [cite: 282, 203]。
* [cite_start]**中空圓環圖 (`MoodPieChart`)：** 月分佈模式下以現代感圓環圖呈現各類心情佔比 [cite: 283, 204]。
* [cite_start]**AI 週/月總結分析：** 系統自動計算「最常出現的心情」(`Dominant Mood`)，並由 Gemini API 給予 25 字以內的週/月心理狀態溫馨總結 [cite: 196]。

### 5. 🔒 多使用者資料隔離系統
* [cite_start]**單機多帳戶隱私防護：** 實作帳號註冊與登入介面，不同的帳號會連接到不同的資料庫 [cite: 38, 197]。
* [cite_start]**資料隔離技術：** 透過全域 `UserManager` 管理狀態，並於 Room Database 核心 `MoodEntity` 中加入 `ownerId` 設計成複合主鍵，確保使用者之間日記完全隔離 [cite: 198, 203]。
* [cite_start]**安全登出機制：** 登出時徹底清空記憶體暫存 (`diaryMap.clear()`)，防止資料殘留 [cite: 198]。

---

## 🛠️ 開發技術棧 (Tech Stack)

* [cite_start]**開發環境：** Android Studio [cite: 203]
* [cite_start]**主要語言：** Kotlin [cite: 195]
* [cite_start]**UI 框架：** Jetpack Compose (自定義 Canvas 繪圖、animateFloatAsState 物理動畫) [cite: 282, 203]
* [cite_start]**本地資料庫：** Room Database (複合主鍵、多帳號架構) [cite: 203]
* [cite_start]**AI 整合：** Google Gemini 2.5-flash API (提示詞工程調校) [cite: 88, 203]
* [cite_start]**多媒體技術：** MediaPlayer (背景音樂與氣泡音效混合播放)、TTS (文字轉語音) 技術 [cite: 203, 207]
* [cite_start]**專案版本控制：** Git / GitHub [cite: 202, 204]

---

## 📈 專案開發里程碑 (Timeline)

* **11/20 | [cite_start]需求分析與構思**：確定「無尾熊樹洞」主題，規劃 App 核心功能與 UI 風格 [cite: 200]。
* **12/01 | [cite_start]介面框架初步成型**：完成主頁面、日曆檢視、心情紀錄三大核心 UI 佈局與導航 [cite: 200]。
* **12/05 | [cite_start]本地資料庫建置**：實作 Room Database 儲存機制，完成日記 CRUD 基本功能 [cite: 200]。
* **12/08 | [cite_start]AI 核心技術導入**：整合 Gemini 2.5-flash，調校「無尾熊語氣」Prompt 核心邏輯 [cite: 200]。
* **12/10 | [cite_start]視覺動畫與多媒體**：加入無尾熊揮手動畫、彈跳音效、背景音樂與 TTS 語音 [cite: 200]。
* **12/15 | [cite_start]數據統計優化**：實作 Canvas 五維雷達圖與圓環圖，優化週/月統計邏輯與 AI 自動化趨勢分析 [cite: 200]。
* **12/16 | [cite_start]使用者系統升級**：實作本地端多使用者隔離架構 (`OwnerId` 機制) 與登入註冊介面 [cite: 200]。
* **12/18 | [cite_start]最終測試與除錯**：修正 Git 合併衝突問題，優化系統通知穩定性，完成最終 Debug 版本 [cite: 200]。

---

## 💡 技術挑戰與突破解決方案

1. [cite_start]**AI 情感回饋機械化的挑戰：** * *困難*：初期串接模型時，回覆過於機械化、說教式，缺乏陪伴感 [cite: 203]。
   * [cite_start]*解決*：深入調校 Prompt Engineering，要求 AI 模擬溫柔、具同理心的無尾熊朋友，使用繁體中文、限制在 60 字內給予文學感的暖心鼓勵，成功讓 AI 具備靈魂溫度 [cite: 203]。
2. **單機架構下的多使用者隱私隔離：**
   * *困難*：為了滿足「共用裝置」場景，需在不架設複雜後端的情況下實現資料隱私與隔離 [cite: 203]。
   * [cite_start]*解決*：重新設計 Room Schema，在核心資料表中引入 `ownerId`，並將 `date` 與 `ownerId` 設為複合主鍵 (`Composite Primary Key`)，達成高效、安全的資料隔離 [cite: 203]。
3. **折線圖與數據性質的邏輯謬誤：**
   * [cite_start]*困難*：最初計畫使用折線圖呈現心情，但心情為獨立維度、非線性遞增關係，折線圖會產生誤導 [cite: 203]。
   * *解決*：果斷重構，利用 Compose Canvas API 自行手工繪製「五維雷達圖」，讓心情維度的分布與傾向更科學且直觀地呈現 [cite: 203, 204]。
4. **Git 版本控制與衝突處理：**
   * [cite_start]*困難*：協作收尾階段同時修改核心檔案，導致嚴重的 `Merge Conflict`，曾因 GitHub 線上誤修導致專案結構損毀 [cite: 204]。
   * [cite_start]*解決*：果斷放棄錯誤分支，退回穩定版本並採取「單機 Debug 後重新上傳」的保守策略 [cite: 204][cite_start]。此經驗也促使團隊深刻體會到功能分支 (`Feature Branching`) 開發與 Git Flow 的重要性 [cite: 204]。

---

## 👥 團隊成員與貢獻分配

[cite_start]本專案由兩位成員共同協作完成 [cite: 195]：

| 成員 | 負責項目 | 貢獻佔比 |
| :--- | :--- | :--- |
| **徐靖祐** | [cite_start]主介面開發、物件動畫、頁面配置、日曆與心情系統、雷達圖統計、無尾熊動畫、TTS語音、AI核心邏輯 [cite: 198] | [cite_start]60% [cite: 198] |
| **丁昱翔** | [cite_start]登入系統、心情資料庫建置、週月趨勢分析、圓環圖統計、音效與BGM、定時推送通知 [cite: 198] | [cite_start]40% [cite: 198] |

---
[cite_start]*本專案為大專院校「Android 應用程式專案開發與社群媒體應用」課程之期末實作成果。* [cite: 11]
