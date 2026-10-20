# Kotlin Multiplatform Contest 2026–2027 — Idea Research Report

> Ngày lập: 20/10/2026
> Thời gian còn lại theo trang contest: 49 ngày
> Mục đích: đóng pha tìm kiếm mở, đọc và chấm toàn bộ ý tưởng trong một lượt, sau đó chọn một concept để làm.
> Phạm vi phân tích: cuộc hội thoại trong `report.md`, các project của m ngoại trừ Hexudon, luật và winner Kotlin Multiplatform Contest nhiều năm, cùng landscape sản phẩm hiện tại.

---

## 0. Kết luận nhanh trước khi đọc chi tiết

Ba điều gần như đã chắc chắn:

1. **Không nên làm cả Scholarship Ecosystem trong 49 ngày.** Đây là north star tốt cho một startup, nhưng là scope tệ cho một bài thi solo.
2. **Không nên sao chép bề mặt của CapWords.** `Camera → nhận diện → giải thích → flashcard/collection` đã có rất nhiều sản phẩm.
3. Bài contest phù hợp nhất sẽ là một **wedge** nhỏ với cấu trúc:

```text
Một input cá nhân
        ↓
Một transformation riêng biệt
        ↓
Một kết quả có giá trị, tích lũy được
        ↓
Một khoảnh khắc demo hiểu được trong 10–20 giây
```

### Shortlist hiện tại

| Hạng tạm thời | Concept | Một câu mô tả | Lý do giữ lại |
|---:|---|---|---|
| 1 | **Pathprint** | Overlay hồ sơ của mình lên hành trình thật của một scholar/alumni rồi “fork” thành con đường cá nhân | Gần cảm xúc của Scholarship Ecosystem nhất nhưng không cần marketplace/mentor live |
| 2 | **Unlock** | Thử thay đổi một yếu tố trong hồ sơ và nhìn thấy các cơ hội vừa được mở khóa | Demo cực rõ, rule engine hợp KMP, scope 49 ngày tốt |
| 3 | **Proofbook** | Mỗi kỹ năng trong hồ sơ phải được nối với bằng chứng thật | Giá trị tăng theo thời gian, hợp camera/mobile + desktop, nhưng thị trường ePortfolio khá đông |
| 4 | **FeedbackLoop** | Biến feedback của giáo viên thành các “open loop” và chứng minh mình đã sửa ở bài tiếp theo | Pain nhỏ, thật, độc lập với kho dữ liệu scholarship |
| 5 | **BuildReplay** | Kéo timeline để xem một project lớn lên từ màn hình trắng đến sản phẩm hoàn chỉnh | Demo rất visual, portfolio thể hiện quá trình thay vì chỉ kết quả |
| 6 | **NoticeLens** | Chụp poster/call-for-applications và biến nó thành opportunity card phù hợp với hồ sơ | Gần CapWords nhất, nhưng phải vượt qua cảm giác “OCR + calendar” |

Đây chưa phải quyết định cuối. Điểm số trong report là **công cụ sàng lọc**, không phải phép đo khách quan.

---

## 1. Những gì m thật sự thích

Nguồn chính là [cuộc hội thoại đã cung cấp](/Users/macbook/Downloads/report.md), không phải giả định từ Hexudon.

### 1.1 Hai mốc tham chiếu

| Phẩm chất | CapWords | Scholarship Ecosystem |
|---|---|---|
| Kết quả mong muốn | Học từ chính thế giới quanh mình | Tìm và tiếp cận cơ hội phát triển thật |
| Cá nhân hóa | Đồ vật và trải nghiệm của người dùng | Hồ sơ, mục tiêu, năng lực và hoàn cảnh của người dùng |
| Giá trị tích lũy | Bộ từ vựng gắn với ký ức | Hồ sơ, bằng chứng, thành tích, network và cơ hội |
| Pull của sản phẩm | Interaction mới, đẹp, muốn thử ngay | Giá trị sống lớn, muốn sở hữu lâu dài |
| Điểm yếu khi mang đi thi | Dễ bị sao chép thành “scan X để học Y” | Scope quá lớn, data/network/cold start nặng |

Điều m thích không phải riêng camera, học bổng hay gamification. Pattern sâu hơn là:

> Sản phẩm hiểu một phần đời sống của người dùng, biến nó thành giá trị cá nhân và làm cho một khả năng mới trở nên dễ tiếp cận hơn.

### 1.2 Tín hiệu từ các project của m — không dùng Hexudon

- [Lexia / WordHuntAI](/Users/macbook/Documents/Projects/Apps/lexia/APP_COMPLETION_PLAN.md): camera/gallery → tách vật thể → nhận diện/correct → nội dung song ngữ → collection/SRS/XP. Đây là bằng chứng mạnh nhất về năng lực camera, image pipeline, local-first, learning loop, accessibility và polish.
- [Luckgage](/Users/macbook/Documents/Projects/Apps/luckgage/README.md): ngữ cảnh chuyến đi + thời tiết → gợi ý hành lý; checklist, readiness, reminder, QR transfer, không cần account/backend. Cho thấy m hợp với contextual utility và trải nghiệm self-contained.
- [Trưa Nay Ăn Gì](/Users/macbook/Documents/Projects/Apps/TruaNayAnGi/README.md) và Ăn Gì Hôm Nay: một quyết định nhỏ hằng ngày được biến thành interaction trực quan, vui và dễ demo.
- TroHub: discovery/matching trong một nhu cầu thật.
- Myosotis: sản phẩm có impact, accessibility và vai trò gia đình/người hỗ trợ.

Pattern đáng tin hơn là:

```text
context thật của người dùng
        ↓
transformation trực quan
        ↓
kết quả cá nhân, dùng được ngay
        ↓
collection/progress có giá trị lâu dài
```

### 1.3 Những kiểu ý tưởng đã chứng minh là không hợp

- Công cụ sáng tác nhạc/vẽ/game chỉ vì “creative”.
- Một vấn đề xã hội quá lớn nhưng core loop mơ hồ.
- App chỉ có giá trị khi giáo viên hoặc cả trường triển khai trước.
- Diagram/editor cần giải thích dài mới hiểu.
- Generic AI advisor, chatbot, summarizer hoặc flashcard generator.
- Idea có output không đủ hấp dẫn để làm video contest.
- Bug/solver/optimization idea được suy ra từ Hexudon.

FlowAtlas và ChalkBack đã được thử bằng hội thoại và bị loại vì chính m không thấy hấp dẫn. Không đưa chúng trở lại shortlist.

---

## 2. Constraint thật của contest

Nguồn chính thức: [contest page](https://kotlinconf.com/contest/) và [official rules](https://kotlinconf.com/contest-rules/).

- Contest: 16/09/2026 – 07/12/2026.
- Solo; một người chỉ nộp một bài.
- Project phải được tạo riêng trong contest period; không nộp project cũ.
- KMP và chạy ít nhất hai platform.
- Không nhận library, template hoặc Hello World.
- GitHub public với Apache 2.0, MIT hoặc BSD-3-Clause.
- README đầy đủ, hướng dẫn chạy mọi target và thử key feature.
- Essay tiếng Anh 300 từ.
- Ban tổ chức khuyến khích screencast 3–5 phút.
- Chấm điểm:
  - 40% creativity/novelty.
  - 40% KMP/shared Kotlin.
  - 20% Kotlin coding conventions.

AI được nhắc như một cách có thể tạo thêm lợi thế, **không phải yêu cầu**.

### Hàm ý thiết kế

Một concept mạnh cần trả lời được bốn câu:

1. Người dùng làm gì trong 10 giây đầu?
2. Transformation nào khiến người xem nói “à, hay đấy”?
3. Tại sao cần hai platform chứ không phải chỉ compile cùng một UI hai lần?
4. Phần nào thực sự được chia sẻ bằng Kotlin?

Android + Desktop hiện là cặp an toàn nhất cho phần lớn concept:

- Android: camera, share intent, QR, capture nhanh, notification.
- Desktop: so sánh, canvas/timeline, authoring, review và export.
- Shared: domain model, rules/simulation, persistence, import/export và phần lớn Compose UI.

Tất cả shortlist trong report đều có thể local-first, không cần Firebase hoặc account trong MVP.

---

## 3. Các winner 2023–2026 nói gì?

### 2023

Nguồn: [Kotlin Foundation results](https://kotlinfoundation.org/news/kotlin-multiplatform-contest-results/) và [KotlinConf 2023 contest](https://2023.kotlinconf.com/contest/).

| Project | Core loop | Bài học |
|---|---|---|
| [DoGoodMobile](https://github.com/ABurakk/DoGoodMobile) | Browse cơ hội tình nguyện → xem chi tiết → liên hệ | Dữ liệu local vẫn có thể đủ cho contest nếu pain và flow rõ |
| [VisioZoezi](https://github.com/BKMbigo/VisioZoezi) | Camera quan sát tư thế → phản hồi động tác | Technical hook rất dễ thấy trong video |
| [mini-games](https://github.com/avan1235/mini-games) | Chơi game trên nhiều client với Kotlin server | KMP là một phần bản chất của sản phẩm |

### 2024

Nguồn: [winner spotlight](https://kotlinfoundation.org/news/kotlin-multiplatform-contest-winners-spotlight/) và [KotlinConf 2024 contest](https://2024.kotlinconf.com/contest/).

| Project | Core loop | Bài học |
|---|---|---|
| [College Advisor](https://github.com/patibachleda/CollegeAdvisorMobile) | Chọn college + major → thấy chi phí, loan và thời gian trả nợ | Không làm cả education ecosystem; làm rất tốt một quyết định |
| [Organiks](https://github.com/samAricha/Organiks1_KMP_Server) | Lưu hồ sơ nông trại offline → sync/assistant khi có mạng | Storytelling và offline-first có giá trị |
| [Pixly](https://github.com/etemesi254/Pixly) | Chỉnh ảnh → filter → compare before/after → export | Category quen thuộc vẫn thắng nếu interaction và kỹ thuật nổi bật |

### 2025

Nguồn: [winner announcement](https://kotlinfoundation.org/news/kmp-contest-winners/) và [KotlinConf 2025 contest](https://2025.kotlinconf.com/contest/).

| Project | Core loop | Bài học |
|---|---|---|
| [arandomroom](https://github.com/andreeabirdie/arandomroom) | Chọn hành động → AI tiếp tục text adventure | AI phục vụ một interaction cụ thể |
| [PayFlo](https://github.com/Valerii3/PayFlo) | Chụp bill → mỗi người mô tả món → app semantic-match và chia tiền | Input đời thật → transformation → output hữu ích |
| [TinyComposer](https://github.com/lemcoder/TinyComposer) | Tạo chord/melody → phát → lưu/export WAV | Output nghe được ngay tạo demo mạnh |

### 2026

Nguồn: [previous winners trên trang contest hiện tại](https://kotlinconf.com/contest/).

| Project | Core loop | Bài học |
|---|---|---|
| [Huntit](https://github.com/AdrianIkeaba/huntit) | Nhận vật cần tìm → chụp → leaderboard | Core loop hiểu ngay và có nhịp độ |
| [Clarity](https://github.com/reivaj-gg/Clarity) | Check-in context + cognitive game → biểu đồ insight cá nhân | Dữ liệu tích lũy tạo giá trị sâu hơn theo thời gian |
| [ClipWhisper](https://github.com/Abderrahman44/ClipWhisper_KMP) | Copy trên phone → paste trên desktop qua LAN | Pain nhỏ nhưng thật; KMP reason rất rõ; không cần cloud/AI |

### Pattern xuyên suốt

- Một câu là hiểu core loop.
- Không cần giải quyết vấn đề khổng lồ.
- Category quen thuộc không tự động bị loại; novelty có thể nằm ở interaction/combo/implementation.
- Local/offline xuất hiện nhiều lần.
- KMP phải nằm trong câu chuyện sản phẩm.
- Demo quan trọng hơn số feature.
- AI tốt khi nó biến đổi một input cụ thể; chatbot chung chung không tạo lợi thế.

---

## 4. Market landscape: vùng nào đã đông?

### 4.1 CapWords-like

Các nhánh hiện đã có nhiều sản phẩm:

- Camera → object → vocabulary: [CapWords](https://capwords.app/), [FlagSay](https://flagsay.app/), [Vocam](https://vocam.app/).
- Camera → homework/note → solution/quiz: [Acuity](https://getacuity.app/), [DoubtGo](https://doubtgo.online/), [PupilBot](https://pupilbot.com/) và các tên lớn như Photomath/QANDA.
- Camera → nature → collection/badges: [Seek by iNaturalist](https://www.inaturalist.org/pages/seek_app), [iNaturalist](https://www.inaturalist.org/).
- Camera → personal knowledge map: [Dannelse Scan and Learn](https://dannelse.ai/scan-and-learn) và nghiên cứu [AiGet](https://arxiv.org/abs/2501.16240).

Do đó công thức sau **không đủ mới**:

```text
scan một thứ
→ AI giải thích
→ tạo quiz/flashcard
→ lưu collection và XP
```

Một idea camera mới cần transformation thứ hai: ảnh không chỉ trở thành “nội dung học”, mà phải trở thành một artifact/action riêng của sản phẩm.

### 4.2 Scholarship Ecosystem

Vision của m:

```text
Academic Profile
+ Opportunities
+ Growth Tools
+ Community / Mentor / Alumni
```

đã có nhiều sản phẩm bao phủ gần hết:

- Matching/feed: [ScholarshipOwl](https://scholarshipowl.com/), [Bold.org](https://bold.org/how-it-works/), [Appily](https://www.appily.com/), [Scholarly](https://www.tryscholarly.app/), [GoApply](https://www.goapplyapp.com/).
- Whole profile + opportunities: [Cirkled In](https://www.cirkledin.com/students/), [Colaraz](https://www.colaraz.com/students), [Opportra](https://app.opportra.com/), [nrol](https://nrol.me/how-it-works), [PathFindr](https://www.pathfindr.org/).
- Gap analysis/growth roadmap: [AppGap](https://www.tryappgap.com/), [Ziova](https://www.ziova.ai/), [Summis](https://www.trysummis.com/), [AdMerit](https://admerit.ai/), [Laikan](https://laikan.app/).
- Application workspace: [EliteApply](https://eliteapply.net/), [Next Scholar](https://www.nextscholar.com/match), [ApplyLens AI](https://devpost.com/software/applylens-ai).
- Mentor/alumni: [Me.Mentor](https://mementor.org/), [PathFindr](https://www.pathfindr.org/), [AlumniK](https://www.alumnik.com/) và uStrive.
- Việt Nam cũng có AppLynk, GlowBal, AppLikeEdu, Eduholic.

Lưu ý: đây chủ yếu là feature claim trên website của các sản phẩm, không phải xác nhận độc lập về traction hoặc chất lượng.

### 4.3 Khoảng trống tương đối còn đáng thử

Không có vùng nào chắc chắn “chưa ai làm”. Các interaction ít bị commoditize hơn gồm:

1. **Overlay + fork hành trình thật của người đi trước.**
2. **Counterfactual simulator:** thay một thuộc tính → thấy opportunity nào mở/đóng.
3. **Source diff:** xem requirements/deadline thay đổi như Git diff.
4. **Evidence-to-requirement compiler:** mỗi claim phải được chứng minh bằng artifact thật.
5. **Opportunity pack local qua QR:** counselor/teacher curate, student kiểm tra eligibility trên máy.

---

## 5. Rubric dùng để sàng lọc ý tưởng

Mỗi concept được xem theo sáu câu hỏi:

| Tiêu chí | Câu hỏi |
|---|---|
| Product pull | Nếu thấy trên App Store/Product Hunt, m có muốn tải không? |
| Novelty | Core interaction có khác category hiện tại, hay chỉ thêm AI/UI đẹp? |
| Demo | Người xem có hiểu giá trị trong 10–20 giây không? |
| KMP | Hai platform có vai trò thật và shared Kotlin có đáng kể không? |
| 49-day feasibility | Một người có làm core loop đủ polish trong thời gian còn lại không? |
| Cold-start independence | Demo và giá trị có tồn tại mà không cần user/data/community quy mô lớn không? |

Điểm `/100` dưới đây chỉ là đánh giá tương đối dựa trên sáu tiêu chí này.

---

## 6. Deep dive các concept mạnh nhất

## 6.1 Pathprint — Fork a path, not a person

**Tagline:** *See how someone got there. Fork the path that fits you.*

### Concept

Một scholar/alumni không chỉ có profile và nút “Chat”. Họ kể hành trình bằng các milestone có thứ tự:

```text
IELTS 6.5
→ volunteer project
→ research assistant
→ fail Erasmus lần 1
→ sửa narrative
→ nhận Erasmus
```

Sinh viên overlay hồ sơ của mình lên hành trình:

```text
IELTS 7.0              ✓
Volunteer evidence     ✗
Research experience    ✗
```

Sau đó bấm **Fork Path**, chọn milestone gần nhất phù hợp và tạo route cá nhân. Không copy nguyên hành trình, không dự đoán “khả năng đậu”.

### Demo 10 giây

Mở journey của Linh → overlay profile → các milestone chuyển xanh/xám → bấm `Fork` → app tạo “Next: build one volunteer project with measurable impact”.

### Giá trị

- Biến một câu chuyện truyền cảm hứng thành một con đường có cấu trúc.
- Giảm phụ thuộc vào việc mentor phải online/chat.
- Cho thấy con đường thật có fail, pivot và context, không phải checklist hoàn hảo do AI bịa.

### MVP 49 ngày

- 8–12 journey mẫu, ghi rõ là sample/curated.
- Desktop author journey bằng milestone cards.
- Mobile explore, overlay, fork và track.
- QR/file để chia sẻ journey.
- Không account, chat, social feed hoặc marketplace.

### KMP story

- Desktop: authoring và chỉnh journey timeline.
- Mobile: khám phá, overlay, fork, check-in milestone.
- Shared Kotlin: journey/profile model, matching/overlay engine, fork logic, persistence, import/export và UI.

### Cạnh tranh và rủi ro

[Journey Wall](https://keystothejourney.com/journey-wall) kể real paths; PathFindr/AlumniK match mentor. Chưa thấy interaction `overlay + fork journey` nổi bật trong landscape đã khảo sát, nhưng đây không phải bằng chứng tuyệt đối về originality.

Rủi ro lớn nhất là journey seed phải có chất lượng. Story giả/generic sẽ làm concept mất linh hồn.

**Điểm tạm: 90/100.**
**Verdict:** ứng viên giàu cảm xúc và đúng vision nhất.

---

## 6.2 Unlock — See what one step opens

**Tagline:** *See which doors are open—and the smallest step that opens the next one.*

### Concept

Sinh viên nhập hồ sơ: GPA, ngành, bậc học, quốc tịch, IELTS, experience và một số evidence. App không chỉ đưa ra feed match.

Nó chia opportunity thành:

- Open: đủ hard eligibility.
- Near miss: thiếu đúng một điều kiện có thể cải thiện.
- Locked: vướng điều kiện không thể/khó thay đổi.
- Unknown: rule cần tự kiểm tra nguồn chính thức.

Chế độ **What if?** cho phép thử:

- IELTS 6.5 → 7.0.
- Thêm 12 tháng experience.
- Hoàn thành một project cộng đồng có evidence.

Opportunity cards mở/đóng ngay và app tính milestone khả thi nào mở nhiều cánh cửa nhất.

### Demo 10 giây

Kéo IELTS từ 6.5 lên 7.0 → bốn cánh cửa scholarship sáng lên → “This is the smallest measurable step that unlocks the most opportunities before June.”

### Điều app tuyệt đối không được nói

- Không nói “84% khả năng đậu”.
- Không dùng AI để quyết định eligibility.
- Không đánh đồng đủ điều kiện với cạnh tranh/được nhận.

### MVP 49 ngày

- Profile với 8–12 thuộc tính.
- 30–50 opportunity được curate thủ công, có nguồn và ngày verify.
- Rule engine giải thích được.
- What-if simulator.
- Near-Miss Atlas.
- Next-step list.
- Android + Desktop, local-first.

### KMP story

- Mobile: cập nhật profile, lưu doors, theo dõi action.
- Desktop: compare nhiều opportunity và author/import rule.
- Shared Kotlin: rule graph, eligibility engine, counterfactual simulator, impact ranking, DB và UI.

### Cạnh tranh và rủi ro

Gap analysis đã có ở AppGap, nrol, Ziova và OppPaths. AdMerit/UniPredict có what-if trong admissions. Novelty của Unlock nằm ở **cross-opportunity visualizer**: thay một thuộc tính và nhìn toàn bộ cánh cửa đổi trạng thái.

Nếu phần này không đủ đẹp và trực quan, sản phẩm sẽ rơi trở lại thành một scholarship matcher bình thường.

**Điểm tạm: 89/100.**
**Verdict:** lựa chọn an toàn nhất giữa novelty, demo và khả thi.

---

## 6.3 Proofbook — Claim → Evidence → Requirement

**Tagline:** *Don't list skills. Prove them.*

### Concept

Sinh viên thường ghi “Kotlin, teamwork, leadership” nhưng bằng chứng nằm rải rác trong GitHub, ảnh sự kiện, video demo, feedback và certificate.

Proofbook biến mỗi artifact thành một evidence capsule:

- Tôi đã làm gì?
- Kết quả cụ thể là gì?
- Skill nào thực sự được chứng minh?
- Artifact/link nào đi kèm?
- Ai có thể attest?

Một target opportunity sau đó highlight capsule nào chứng minh từng requirement.

### Demo 10 giây

Import GitHub project hoặc chụp certificate → `Kotlin: Claimed` chuyển thành `Kotlin: Evidenced` → hai requirement trong target application được phủ xanh.

### MVP 49 ngày

- Local evidence cards: image, link, file, outcome và reflection.
- Skill-to-evidence graph.
- Target requirement coverage.
- Export portfolio/application pack thành PDF/ZIP.
- Không verification server; attestation chỉ là stretch goal.

### KMP story

- Mobile: capture evidence đúng lúc nó xảy ra.
- Desktop: tổ chức graph, compare requirement và export pack.
- Shared: model, coverage engine, DB, file metadata và UI.

### Cạnh tranh và rủi ro

ePortfolio/evidence đã có ở Cirkled In, Portflow Mobile, Pocket Shift, AttestEd, Opportra và Wayfinder. Proofbook chỉ đáng làm nếu interaction cốt lõi là **evidence-to-requirement compiler**, không phải một CV builder hoặc gallery thành tích.

**Điểm tạm: 82/100.**
**Verdict:** rất hợp gu tích lũy cá nhân, nhưng novelty cần bảo vệ kỹ.

---

## 6.4 FeedbackLoop — Make feedback visible across assignments

**Tagline:** *Feedback should close a loop, not disappear in an old file.*

### Concept

Sinh viên nhận comment trong bài cũ nhưng thường không có cơ chế chứng minh đã áp dụng ở bài mới.

- Chụp/import feedback của giáo viên.
- Mỗi comment thành một open loop.
- Ở revision hoặc assignment tiếp theo, sinh viên gắn vùng/artifact chứng minh đã sửa.
- Timeline cho thấy feedback nào lặp lại, feedback nào đã được đóng.

### Demo 10 giây

Chạm comment “Explain why this design was chosen” → kéo sang đoạn rationale trong bài mới → loop chuyển từ đỏ sang xanh → history hiện ba lần cải thiện.

### Giá trị

- Sinh viên biến feedback thành growth record.
- Giáo viên thấy feedback có được áp dụng hay không.
- Không cần AI, kho scholarship hoặc cộng đồng lớn.

### MVP 49 ngày

- Import ảnh/PDF hoặc tạo feedback card thủ công.
- Open/resolved loop.
- Link loop với evidence trong revision mới.
- Before/after view và growth timeline.
- Export review pack.

### KMP story

- Mobile: chụp feedback giấy và check nhanh.
- Desktop: review hai version, link evidence, export.
- Shared: feedback state machine, artifact links, stats, DB và UI.

### Rủi ro

Nếu chỉ là to-do list từ feedback thì không mới. Magic interaction phải là **cross-assignment evidence of improvement**.

**Điểm tạm: 83/100.**
**Verdict:** wildcard độc lập với scholarship đáng giữ.

---

## 6.5 BuildReplay — Show the making, not only the result

**Tagline:** *Your portfolio shows what you built. BuildReplay shows how you became able to build it.*

### Concept

Git history khó đọc với người ngoài; portfolio thường chỉ cho thấy screenshot cuối. BuildReplay lưu:

- screenshot mỗi milestone;
- quyết định thiết kế;
- bug quan trọng;
- feedback;
- before/after;
- bài học và pivot.

### Demo 10 giây

Kéo timeline từ màn hình trắng → prototype xấu → bug → feedback → app hoàn chỉnh; mỗi milestone bật ra một decision/evidence card.

### MVP 49 ngày

- Manual project snapshot.
- Milestone timeline.
- Before/after slider.
- Decision/bug/feedback cards.
- Playback mode và export project story.
- Không phân tích Git tự động trong MVP.

### KMP story

- Desktop: capture/import screenshot, author timeline.
- Mobile: xem/present, thêm ảnh hoặc ghi chú khi làm việc ngoài máy.
- Shared: project model, playback engine, image metadata, persistence, export và UI.

### Rủi ro

Nếu chỉ là slideshow đẹp thì giá trị yếu. Phải làm nổi bật development process, decisions và evidence, không chỉ gallery screenshot.

**Điểm tạm: 82/100.**
**Verdict:** demo đẹp nhất trong nhóm portfolio, nhưng xa Scholarship hơn Pathprint/Unlock.

---

## 6.6 NoticeLens — Capture an opportunity before it disappears

**Tagline:** *Point at an opportunity. Know what it means for you.*

### Concept

Sinh viên thấy poster học bổng, hackathon, workshop hoặc internship, chụp lại rồi quên trong gallery.

NoticeLens biến poster/page/screenshot thành opportunity card:

- title, deadline, organizer và official source;
- hard eligibility;
- documents cần chuẩn bị;
- match với profile;
- save, reminder và QR share.

### Demo 10 giây

Camera nhìn poster Kotlin contest → card xuất hiện → `Eligible`, `49 days left`, `Missing: public GitHub repository` → save vào collection.

### MVP 49 ngày

- Crop/perspective correction.
- OCR cơ bản và user confirmation.
- Structured opportunity card.
- Local collection và reminders.
- Transparent profile rules.
- QR/file sharing.

### KMP story

- Mobile: capture/share intent, OCR, reminder.
- Desktop: review, edit và compare opportunity.
- Shared: opportunity schema, validation, eligibility rules, DB và UI.

### Cạnh tranh và rủi ro

Photo-to-calendar đã đông; ApplyLens và các AI eligibility tools đã có. Nếu pitch là “AI đọc poster”, concept sẽ yếu. Giá trị phải là:

```text
capture → verify → personal eligibility → follow-through
```

chứ không chỉ OCR.

**Điểm tạm: 78/100.**
**Verdict:** gần CapWords nhất về interaction, nhưng novelty thấp hơn cảm giác ban đầu.

---

## 6.7 SourceDiff — Git diff for opportunities

**Tagline:** *Know exactly what changed before you apply.*

### Concept

User lưu official page/PDF của một số scholarship mục tiêu. App cho thấy:

- deadline đổi;
- IELTS/GPA criterion đổi;
- document mới xuất hiện;
- benefit/funding bị sửa;
- before/after có highlight như Git diff.

### Demo 10 giây

Mở Rhodes 2026 vs 2027 → `Deadline: changed`, `Reference letters: 2 → 3`, `Eligible regions: modified` hiện bằng visual diff.

### MVP 49 ngày

- Import hai snapshot HTML/text/PDF đã chuẩn bị.
- Structured field diff + raw text diff.
- Watchlist và local notification fixture.
- Không cần backend monitoring thật trong MVP.

### Rủi ro

Demo fixture dễ bị cảm giác giả; monitoring thực cần scheduler/backend và parsing web rất dễ vỡ. Giá trị cho người dùng hẹp hơn các concept trên.

**Điểm tạm: 76/100.**
**Verdict:** interaction khác biệt, nhưng technical/product risk cao hơn.

---

## 6.8 Rubric Mirror — Show the evidence before you submit

**Tagline:** *Don't ask “Did I finish?” Ask “Where is the proof?”*

### Concept

Trước khi nộp bài, sinh viên phải gắn mỗi rubric criterion vào đúng vùng hoặc artifact chứng minh rằng mình đã đáp ứng.

### Demo 10 giây

Rubric readiness 40% → kéo screenshot test vào “Evidence of validation” → kéo README vào “Reproducibility” → readiness tăng 75%, criterion không có evidence vẫn đỏ.

### MVP

- Teacher desktop tạo/import rubric.
- Student mobile/desktop attach evidence.
- Readiness view và missing-evidence list.
- QR/file handoff; không làm LMS.

### Rủi ro

Category rubric/LMS quen thuộc. Novelty phải nằm ở interaction `criterion ↔ exact evidence`, không phải chấm điểm tự động.

**Điểm tạm: 80/100.**
**Verdict:** dễ làm sạch và có value thật, nhưng product pull thấp hơn Pathprint/Unlock.

---

## 7. Portfolio rộng: các ý tưởng còn lại

### 7.1 Scholarship / opportunity wedges

| Ý tưởng | Demo 10 giây | Điểm mạnh | Rủi ro / lý do chưa vào Top 6 | Điểm |
|---|---|---|---|---:|
| **Near-Miss Atlas** | Chỉ hiện opportunity thiếu đúng một điều kiện có thể cải thiện | Rất rõ và thực tế | Tốt hơn khi là mode của Unlock | 79 |
| **ApplyBlocks** | Mở target mới, 7/10 requirement tự được phủ bởi evidence/story đã có | Tái sử dụng công sức application | Dễ thành Notion template | 77 |
| **Reference Pack** | Một nút xuất brag sheet, evidence, deadlines cho recommender | Pain cụ thể cho student/teacher | App BragSheet và Common App template đã tồn tại | 74 |
| **Opportunity Pack QR** | Teacher tạo pack, cả lớp scan và check local | Privacy, KMP và offline tốt | Novelty trung bình | 76 |
| **Opportunity Relay** | Hai sinh viên chạm QR để truyền opportunity card | Dễ demo, không account | Chia sẻ không phải value cốt lõi đủ lớn | 71 |
| **Scholarship Review Lab** | User đóng vai reviewer, xếp application theo rubric | Giúp hiểu góc nhìn hội đồng | Cần sample application tốt; privacy/copyright | 73 |
| **Deadline Terrain** | Dependency map tự backcast từ deadline | Utility rõ | Novelty thấp, dễ thành planner | 70 |
| **Funding Compare** | Chọn hai chương trình, app so total cost/funding/conditions | Hữu ích như College Advisor | Data quốc tế khó chuẩn hóa và cập nhật | 69 |
| **Application Postmortem** | Sau kết quả, timeline cho thấy bước nào trượt và bài học | Giá trị tích lũy | Feedback thật thường không có; demo yếu | 66 |
| **Eligibility Receipt** | Mỗi match có receipt giải thích rule và source | Tăng trust | Nên là feature bắt buộc của Unlock | 75 |

### 7.2 CapWords-like: capture → transformation → collection

| Ý tưởng | Demo 10 giây | Điểm mạnh | Rủi ro / lý do chưa vào Top 6 | Điểm |
|---|---|---|---|---:|
| **UX Safari** | Chụp UI xấu → mark dark pattern/a11y issue → atlas cá nhân | Visual, đúng sinh viên CNTT | Niche và gần field notebook | 75 |
| **Data Detective** | Chụp biểu đồ/quảng cáo → đánh dấu cách số liệu gây hiểu lầm | Data literacy, audience rộng | Recognition/analysis dễ thành AI wrapper | 72 |
| **Mistake Atlas** | Chụp lỗi từ bài/code → pattern lặp lại sáng lên | Personal, tích lũy | Bug diary/error notebook đã có adjacency | 73 |
| **Campus Lab Passport** | Scan thiết bị → hoàn thành procedure → competency stamp | Tạo năng lực thật | Phụ thuộc lab/trường triển khai | 71 |
| **Project Specimens** | Chụp project → specimen card gồm stack, decision, failure, lesson | Collection đẹp | Trùng vùng Proofbook/BuildReplay | 74 |
| **Accessibility Field Notes** | Chụp barrier → annotate guideline và proposed fix | Có ý nghĩa, visual | Dễ thành issue tracker hoặc citizen-report app | 70 |
| **Command Cards** | Lưu terminal command với input/output/use case | Hữu ích cho CNTT | Cookbook/snippet manager rất đông | 65 |
| **Algorithm in the Wild** | Chụp hàng chờ/cửa xoay → lưu queue/state example | Tinh thần CapWords | Đã thử dưới dạng FlowAtlas và khó giải thích | 61 |

### 7.3 Vấn đề nhỏ giữa sinh viên và giáo viên

| Ý tưởng | Demo 10 giây | Điểm mạnh | Rủi ro / lý do chưa vào Top 6 | Điểm |
|---|---|---|---|---:|
| **StuckMap** | Nhiều student chạm cùng dòng code → heatmap đỏ trên desktop | KMP rất rõ, realtime demo tốt | Cần nhiều device/session; adoption theo lớp | 79 |
| **Hand-in Preflight** | Kéo project vào → thiếu README/demo/citation hiện đỏ | Pain thật, scope nhỏ | Rule phụ thuộc từng assignment; novelty vừa | 76 |
| **SkillReceipt** | Mentor ký một micro-achievement bằng QR → proof verified | Two-device demo mạnh | Credential/verification là category đã có | 76 |
| **Critique Tokens** | Feedback buộc gắn đúng vùng artifact và loại critique | Peer review cụ thể hơn comment chung | Cần network lớp học | 70 |
| **Office Hour Packet** | Student gửi “đã thử gì / kẹt đâu / cần gì” trước buổi gặp | Tiết kiệm thời gian thật | Demo và visual pull yếu | 68 |
| **DemoQueue** | QR join, timer, peer feedback cho buổi bảo vệ | Dễ triển khai trong lớp | Commodity, novelty thấp | 64 |
| **Absence Packet** | Bạn học gói slide + ảnh + ba ý chính cho người nghỉ | Pain dễ hiểu | Phụ thuộc altruism, khó duy trì | 62 |

---

## 8. Những ý tưởng nên loại hẳn

| Hướng | Lý do loại |
|---|---|
| Full Scholarship Ecosystem | Profile + feed + growth + mentor + alumni + community là roadmap nhiều năm, không phải MVP 49 ngày |
| AI scholarship matcher | Thị trường đông; dễ bị thay bằng ChatGPT + spreadsheet |
| AI profile score 0–100 | AppGap, Ziova, Summis, nrol đã có; score khó đáng tin |
| AI essay/CV reviewer | Generic wrapper; khó chứng minh chất lượng và novelty |
| Mentor/alumni marketplace | Verification, safety, moderation, availability và cold start quá nặng |
| Peer skill swap | Cần mật độ user và đã có nhiều sản phẩm tương tự |
| Generic study assistant / summarizer / flashcard | Category quá đông, KMP reason yếu |
| CapWords clone cho một subject khác | Derivative; camera + label + collection không đủ mới |
| Homework scanner | Photomath/QANDA và nhiều app mới đã chiếm vùng này |
| QR attendance / mini LMS | Dễ build nhưng commodity |
| Bug diary / CounterLab | Không còn căn cứ phù hợp sau khi loại Hexudon |
| FlowAtlas | Mất quá lâu để giải thích; output là diagram không hấp dẫn |
| ChalkBack | User không có pull; camera stability risk cao |
| Campus super-app | Nhiều module nhưng không có core interaction đáng nhớ |
| Automatic video-to-notes | Technical risk lớn; dễ thành một AI demo |

---

## 9. Có nên kết hợp các concept không?

### Product vision dài hạn có thể là

```text
Proofbook: Tôi đã làm và chứng minh được gì?
        ↓
Pathprint: Người đi trước đã đi bằng con đường nào?
        ↓
Unlock: Từ vị trí hiện tại, bước nào mở thêm cơ hội?
        ↓
Opportunities: Tôi nên apply gì ngay bây giờ?
```

Đây là một Scholarship Ecosystem có logic tốt hơn danh sách module ban đầu.

### Nhưng submission contest chỉ được chọn một lõi

- Chọn **Pathprint** nếu muốn concept cảm xúc, story và discovery.
- Chọn **Unlock** nếu muốn interaction dễ demo và rule engine mạnh.
- Chọn **Proofbook** nếu muốn tài sản cá nhân sống lâu sau contest.

Không build cả ba trước deadline. Có thể thiết kế data model để mở rộng sau, nhưng video contest chỉ kể một story.

---

## 10. So sánh trực tiếp ba lựa chọn gần gu nhất

| Câu hỏi | Pathprint | Unlock | Proofbook |
|---|---|---|---|
| “T có muốn tải không?” | Cao nếu journey thật và đẹp | Cao nếu catalog liên quan | Cao với người đang build profile |
| Magic moment | Overlay + fork một hành trình | Một thay đổi mở nhiều doors | Claim chuyển thành Evidenced |
| Giá trị tích lũy | Route và milestone cá nhân | Profile + action + unlocked opportunities | Evidence graph sống lâu dài |
| Novelty tương đối | Cao nhất | Trung bình-khá | Trung bình |
| Demo | Rõ, giàu cảm xúc | Rõ nhất | Rõ nếu visual coverage tốt |
| Data seed | 8–12 journey chất lượng | 30–50 opportunity + rules | User tự tạo artifact |
| Cold start | Kiểm soát bằng curated sample | Kiểm soát bằng local catalog | Gần như không có |
| Rủi ro lớn nhất | Story giả/generic | Trở thành matcher bình thường | Trở thành ePortfolio/CV builder |
| KMP | Desktop author + mobile fork | Desktop compare + mobile track | Mobile capture + desktop organize |
| Khả thi 49 ngày | Khá cao | Cao nhất | Cao |

---

## 11. Phiếu đọc và chấm của m

Đừng chấm bằng câu “có thể làm được không?” trước. Chấm product pull trước.

Thang điểm:

- `0`: không quan tâm.
- `1`: hiểu nhưng không muốn dùng.
- `2`: có thể hữu ích cho người khác.
- `3`: có hứng thú nhẹ.
- `4`: muốn thử nếu thấy trên App Store.
- `5`: muốn sở hữu và muốn tự build.

| Concept | Hiểu trong 10 giây | Muốn tải | Muốn build 49 ngày | Demo làm m phấn khích | Ghi chú |
|---|---:|---:|---:|---:|---|
| Pathprint |  |  |  |  |  |
| Unlock |  |  |  |  |  |
| Proofbook |  |  |  |  |  |
| FeedbackLoop |  |  |  |  |  |
| BuildReplay |  |  |  |  |  |
| NoticeLens |  |  |  |  |  |
| SourceDiff |  |  |  |  |  |
| Rubric Mirror |  |  |  |  |  |
| Near-Miss Atlas |  |  |  |  |  |
| ApplyBlocks |  |  |  |  |  |
| Reference Pack |  |  |  |  |  |
| Opportunity Pack QR |  |  |  |  |  |
| Scholarship Review Lab |  |  |  |  |  |
| UX Safari |  |  |  |  |  |
| Data Detective |  |  |  |  |  |
| Mistake Atlas |  |  |  |  |  |
| Project Specimens |  |  |  |  |  |
| StuckMap |  |  |  |  |  |
| Hand-in Preflight |  |  |  |  |  |
| SkillReceipt |  |  |  |  |  |

### Ba câu loại ngay

Với mỗi concept được 4–5 điểm, hỏi tiếp:

1. Nếu bỏ AI hoàn toàn, sản phẩm còn đáng tồn tại không?
2. Nếu chỉ có dữ liệu sample/local, demo vẫn thuyết phục không?
3. Có thể kể trọn core loop trong video 30 giây đầu không?

Nếu câu trả lời là “không” ở hai câu trở lên, loại.

---

## 12. Quy trình chốt để không tìm ý tưởng vô hạn

### 20/10 — đọc report

- Đánh điểm toàn bộ bảng trên.
- Không research thêm trong lúc chấm.
- Chỉ giữ tối đa 5 concept.

### 21/10 — rút còn 3

Với mỗi concept, tự viết lại bằng lời của m:

```text
Cho [ai]
khi [tình huống cụ thể]
app biến [input]
thành [output đáng muốn]
bằng [interaction riêng].
```

Nếu không viết được trong 2–3 câu, loại.

### 22/10 — storyboard thay vì code

Mỗi concept còn lại chỉ được ba frame:

1. Before / problem.
2. Magic interaction.
3. Valuable result.

Không thêm settings, auth, community hoặc AI chat.

### 23/10 — chọn một

Chọn concept có tổng tốt nhất của:

- m thật sự muốn dùng;
- demo mạnh;
- KMP tự nhiên;
- technical risk thấp;
- novelty còn bảo vệ được sau competitor check.

Sau ngày này chỉ đổi idea nếu phát hiện blocker nghiêm trọng, không đổi vì xuất hiện một idea mới “nghe cũng hay”.

---

## 13. Kế hoạch 49 ngày sau khi chốt

| Giai đoạn | Ngày | Outcome bắt buộc |
|---|---:|---|
| Chọn và khóa idea | 1–3 | One-liner, storyboard, scope và kill list |
| Technical spike | 4–7 | Chứng minh phần rủi ro nhất chạy được trên ít nhất một platform |
| Core loop | 8–21 | Input → transformation → output hoàn chỉnh, local-first |
| Product depth | 22–28 | Collection/history/progress làm giá trị tích lũy |
| Platform thứ hai | 29–35 | Hai target chạy, shared Kotlin rõ ràng |
| Polish + tests | 36–42 | Empty/error/loading, accessibility, tests, coding conventions |
| Submission assets | 43–46 | README English, install instructions, screenshots, essay 300 từ, video draft |
| Buffer | 47–49 | Clone sạch, chạy lại key flow, quay video cuối và submit sớm |

### Scope rule

Nếu một feature:

- không xuất hiện trong video 3–5 phút;
- không tăng creativity/KMP/code quality;
- hoặc cần account/backend/network effect chỉ để nhìn “thật”;

thì để sau contest.

---

## 14. Khuyến nghị hiện tại

Nếu phải quyết định ngay từ bằng chứng đang có:

### Lựa chọn A — Pathprint

Chọn nếu thứ hấp dẫn m ở Scholarship Ecosystem là **nhìn thấy những khả năng sống thật của người khác và biến chúng thành con đường cho mình**.

### Lựa chọn B — Unlock

Chọn nếu thứ hấp dẫn m là **thấy rõ mình đang ở đâu và bước nhỏ nào mở thêm nhiều cơ hội nhất**.

### Lựa chọn C — Proofbook

Chọn nếu thứ hấp dẫn m là **một hồ sơ lớn lên từ những điều mình thật sự đã làm**, gần cảm giác collection của CapWords nhất mà không dùng camera như một gimmick.

Khuyến nghị chiến lược của report:

> Đọc và chấm Pathprint, Unlock, Proofbook trước. Nếu cả ba đều dưới 4/5 về “muốn tải”, chuyển sang FeedbackLoop hoặc BuildReplay; không quay lại full Scholarship Ecosystem.

---

## 15. Nguồn nghiên cứu chính

### Contest

- [Kotlin Multiplatform Contest 2026–2027](https://kotlinconf.com/contest/)
- [Official Rules](https://kotlinconf.com/contest-rules/)
- [Kotlin Foundation — 2023 results](https://kotlinfoundation.org/news/kotlin-multiplatform-contest-results/)
- [Kotlin Foundation — 2024 winner spotlight](https://kotlinfoundation.org/news/kotlin-multiplatform-contest-winners-spotlight/)
- [Kotlin Foundation — 2025 winners](https://kotlinfoundation.org/news/kmp-contest-winners/)

### Scholarship/opportunity landscape

- [Cirkled In](https://www.cirkledin.com/students/)
- [Colaraz](https://www.colaraz.com/students)
- [nrol](https://nrol.me/how-it-works)
- [Opportra](https://app.opportra.com/)
- [PathFindr](https://www.pathfindr.org/)
- [EnsureCollege](https://ensurecollege.com/)
- [GoApply](https://www.goapplyapp.com/)
- [EliteApply](https://eliteapply.net/)
- [ApplyLens AI](https://devpost.com/software/applylens-ai)
- [AdMerit](https://admerit.ai/)

### Evidence/portfolio and CapWords adjacency

- [CapWords](https://capwords.app/)
- [Seek by iNaturalist](https://www.inaturalist.org/pages/seek_app)
- [Wayfinder student artifacts](https://wayfinder.zendesk.com/hc/en-us/articles/38097410598676-Portrait-of-a-Graduate-Student-Artifacts)
- [Common App brag sheet](https://www.commonapp.org/static/7bc36ad35601e024c5ba48dcef1292e2/Resource_FY_TeacherBragSheet_ENG_2025.06.25_1.pdf)
- [Portflow Mobile](https://drieam.com/en/solutions/portflow/mobile-app/)

---

## 16. Điều report này không khẳng định

- Không khẳng định bất kỳ concept nào là chưa từng có trên thế giới.
- Không coi feature claim trên website đối thủ là bằng chứng họ đã thực thi tốt hoặc có traction.
- Không dự đoán concept nào chắc chắn thắng contest.
- Không dùng Hexudon để suy ra sở thích hoặc lợi thế của m.
- Không khuyên reuse code/project cũ; submission phải được tạo riêng cho contest.

Mục tiêu của tài liệu là biến việc lựa chọn từ một cuộc trò chuyện vô hạn thành một quyết định có tiêu chí, có bằng chứng và có deadline.
