# Daily Mood 考拉日記 🐨

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![Gemini AI](https://img.shields.io/badge/AI-Gemini%202.5%20Flash-F9AB00?style=flat-square&logo=googlegemini&logoColor=white)

> **「Daily Mood」不只是一個課程專案，更是一個具備溫度的情緒陪伴工具。**
> 我們以慢活、療癒的「無尾熊」作為核心，將 App 定義為一個溫溫柔柔的樹洞，讓你的每一份心情都能得到即時的溫暖回應。

---

## 📌 專案製作動機

在課業、人際、家庭或情感的多重壓力下，現代人的生活節奏往往快得讓人沒有時間檢視自己一整天的心情，堆積已久的負面感受容易導致心理健康問題。

市面上的心情日記大多像冷冰冰的記帳本，純粹紀錄缺乏動力。因此，我們開發了「考拉日記」，提供一個溫暖、無壓力的互動空間。專案結合了 **Google Gemini AI 技術**，模擬慵懶、溫柔的無尾熊語調進行即時情感回饋，並提供豐富的數據視覺化圖表，提升使用者對自我心理狀態的認知。

---

## 🚀 核心功能介紹

### 1. 🐨 首頁互動與沉浸式體驗
* **動態無尾熊：** 點擊首頁無尾熊會觸發可愛的搖晃與揮手動畫，並伴隨打招呼的文字與語音。
* **五感沉浸環境：** 全介面按鈕（除無尾熊外）均點擊內建特製「氣泡音」音效，並搭配舒適的背景音樂 (BGM) 與動態物理基礎果凍動畫。
* **定時推送通知：** 每天固定時間（21:00）貼心提醒，邀請你停下腳步記錄今天的生活。

### 2. ✍️ 直覺式心情日記與 Gemini AI 智慧回饋
* **直覺心情選擇：** 提供 5 種專屬無尾熊表情按鈕（開心、難過、生氣、興奮、平靜）。
* **Gemini 2.5-flash 情感分析：** 儲存日記時，AI 會化身為充滿同理心的無尾熊朋友，根據日記文字與心情，在 60 字內生成一段溫柔、文學感且專屬的療癒小語。

### 3. 📅 互動式日曆檢視介面
* **心情色點標記：** 日曆會自動掃描當月資料，在寫過日記的日期下方繪製對應心情顏色的圓點（如粉紅代表開心、深紅代表生氣），一目了然整月情緒。
* **互動詳情卡片：** 點擊過去日期會透過平滑動畫 (`SmoothEntranceAnim`) 滑出當天日記詳情並支援內容修改。
* **防呆機制：** 針對「未來」的日期會鎖定修改按鈕，防止錯誤操作。

### 4. 📊 進階 Canvas 數據統計分析
* **雙重維度分析：** 支援透過分段控制項 (`Segmented Control`) 切換「本週趨勢」與「本月分佈」。
* **五維心情雷達圖 (`MoodRadarChart`)：** 捨棄傳統折線圖，利用自定義 Canvas 繪製雷達圖，將五種情緒量化，直觀反映情緒傾向。
* **中空圓環圖 (`MoodPieChart`)：** 月分佈模式下以現代感圓環圖呈現各類心情佔比。
* **AI 週/月總結分析：** 系統自動計算「最常出現的心情」(`Dominant Mood`)，並由 Gemini API 給予 25 字以內的週/月心理狀態溫馨總結。

### 5. 🔒 多使用者資料隔離系統
* **單機多帳戶隱私防護：** 實作帳號註冊與登入介面，不同的帳號會連接到不同的資料庫。
* **資料隔離技術：** 透過全域 `UserManager` 管理狀態，並於 Room Database 核心 `MoodEntity` 中加入 `ownerId` 設計成複合主鍵，確保使用者之間日記完全隔離。
* **安全登出機制：** 登出時徹底清空記憶體暫存 (`diaryMap.clear()`)，防止資料殘留。
* 
---

## 🛠️ 開發技術棧 (Tech Stack)

* **開發環境：** Android Studio
* **主要語言：** Kotlin
* **UI 框架：** Jetpack Compose (自定義 Canvas 繪圖、animateFloatAsState 物理動畫)
* **本地資料庫：** Room Database (複合主鍵、多帳號架構)
* **AI 整合：** Google Gemini 2.5-flash API (提示詞工程調校)
* **多媒體技術：** MediaPlayer (背景音樂與氣泡音效混合播放)、TTS (文字轉語音) 技術
* **專案版本控制：** Git / GitHub

---

## 📈 專案開發里程碑 (Timeline)

* **11/20 | 需求分析與構思**：確定「無尾熊樹洞」主題，規劃 App 核心功能與 UI 風格。
* **12/01 | 介面框架初步成型**：完成主頁面、日曆檢視、心情紀錄三大核心 UI 佈局與導航。
* **12/05 | 本地資料庫建置**：實作 Room Database 儲存機制，完成日記 CRUD 基本功能。
* **12/08 | AI 核心技術導入**：整合 Gemini 2.5-flash，調校「無尾熊語氣」Prompt 核心邏輯。
* **12/10 | 視覺動畫與多媒體**：加入無尾熊揮手動畫、彈跳音效、背景音樂與 TTS 語音。
* **12/15 | 數據統計優化**：實作 Canvas 五維雷達圖與圓環圖，優化週/月統計邏輯與 AI 自動化趨勢分析。
* **12/16 | 使用者系統升級**：實作本地端多使用者隔離架構 (`OwnerId` 機制) 與登入註冊介面。
* **12/18 | 最終測試與除錯**：修正 Git 合併衝突問題，優化系統通知穩定性，完成最終 Debug 版本。

---

## 💡 技術挑戰與突破解決方案

1. **AI 情感回饋機械化的挑戰：**
   * *困難*：初期串接模型時，回覆過於機械化、說教式，缺乏陪伴感。
   * *解決*：深入調校 Prompt Engineering，要求 AI 模擬溫柔、具同理心的無尾熊朋友，使用繁體中文、限制在 60 字內給予文學感的暖心鼓勵，成功讓 AI 具備靈魂溫度。
2. **單機架構下的多使用者隱私隔離：**
   * *困難*：為了滿足「共用裝置」場景，需在不架設複雜後端的情況下實現資料隱私與隔離。
   * *解決*：重新設計 Room Schema，在核心資料表中引入 `ownerId`，並將 `date` 與 `ownerId` 設為複合主鍵 (`Composite Primary Key`)，達成高效、安全的資料隔離。
3. **折線圖與數據性質的邏輯謬誤：**
   * *困難*：最初計畫使用折線圖呈現心情，但心情為獨立維度、非線性遞增關係，折線圖會產生誤導。
   * *解決*：果斷重構，利用 Compose Canvas API 自行手工繪製「五維雷達圖」，讓心情維度的分布與傾向更科學且直觀地呈現。
4. **Git 版本控制與衝突處理：**
   * *困難*：協作收尾階段同時修改核心檔案，導致嚴重的 `Merge Conflict`，曾因 GitHub 線上誤修導致專案結構損毀。
   * *解決*：果斷放棄錯誤分支，退回穩定版本並採取「單機拉取（Pull）與合併後再上傳」的標準策略，避免盲目使用強行覆蓋指令 (`push -f`) 抹除夥伴進度。

---

## 👥 團隊成員與貢獻分配

本專案由兩位成員共同協作完成：

| 成員 | 負責項目 | 貢獻佔比 |
| :--- | :--- | :--- |
| **徐靖祐** | 主介面開發、物件動畫、頁面配置、日曆與心情系統、雷達圖統計、無尾熊動畫、TTS語音、AI核心邏輯 | 60% |
| **丁昱翔** | 登入系統、心情資料庫建置、週月趨勢分析、圓環圖統計、音效與BGM、定時推送通知 | 40% |

---
*本專案為大專院校「Android 應用程式專案開發與社群媒體應用」課程之期末實作成果。*

<img width="466" height="919" alt="image" src="https://github.com/user-attachments/assets/9608adbb-71d8-4130-b547-d26039c475b3" />
<img width="346" height="680" alt="image" src="https://github.com/user-attachments/assets/59bc4cb3-e98b-4385-ba3d-c0a370a3bca0" />
<img width="289" height="282" alt="image" src="https://github.com/user-attachments/assets/25eb87f0-8bf9-447d-8d02-11f2a212097d" />
<img width="355" height="699" alt="image" src="https://github.com/user-attachments/assets/a590d5d8-aadc-404e-b537-bb1f569482b1" />
<img width="506" height="717" alt="image" src="https://github.com/user-attachments/assets/cab40d08-ebed-4b39-8cb7-657f590bb9a3" />
<img width="401" height="701" alt="image" src="https://github.com/user-attachments/assets/bf3d00ad-73ce-477f-b57d-65c3ef1f8bc4" />
<img width="608" height="361" alt="image" src="https://github.com/user-attachments/assets/b09932b1-06da-43d8-b52d-19b60352694f" />
<img width="305" height="386" alt="image" src="https://github.com/user-attachments/assets/c691f3b3-2f48-4496-b46f-e2f169ffaacd" />
