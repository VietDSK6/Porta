# PORTA — Product, UX & Core Logic Audit

> Historical AI-assisted audit. Retained for provenance; findings may have been superseded by later implementation.

**Bản đánh giá độc lập phục vụ cải tiến MVP / Kotlin Multiplatform contest**  
**Ngày:** 24/10/2026  
**Nguồn:** `PORTA_PRODUCT_CONTEXT.md` (snapshot static source trace 24/10/2026, 1.129 dòng); ảnh chụp onboarding Android và desktop, các màn Doors, Next moves, Profile, Edit academic profile do chủ sản phẩm cung cấp trong cuộc trò chuyện.  
**Trạng thái:** Bản audit định hướng và backlog. **Chưa** trực tiếp clone/chạy repository, thao tác app, thực thi tests, xác minh live URLs hoặc phỏng vấn người dùng.

> **Phân biệt bằng chứng.** `[CODE-REPORT]`: thông tin được báo cáo bối cảnh dẫn từ source code; chưa tự kiểm tra trực tiếp. `[SCREEN]`: quan sát từ screenshot; không chứng minh hành vi runtime. `[PRODUCT]`: phân tích/đề xuất của reviewer. `[VERIFY]`: điều chưa chắc chắn, cần chứng thực bằng source, chạy app hoặc nguồn chính thức. Các chỉ dẫn bên dưới không được coi là kết luận về tính hợp lệ của bất kỳ học bổng/chương trình cụ thể nào.

---

## 0. Executive decision memo

### Kết luận ngắn

**Porta đã có một engine đáng kể nhưng chưa có một _real-data vertical slice_ đủ sức thể hiện product promise.** Hệ thống deterministic eligibility, nguồn/cycle, scoped assertions, simulation, Pareto và local persistence là nền tảng tốt. Tuy nhiên, bộ 15 cơ hội thực tế quá nhiều manual/unknown; nhiều fact thu thập không tham gia real rules; action chiếm ưu thế là bốn bước xác nhận availability riêng rẽ, có duration 14 ngày do Porta gán. Vì thế người dùng được chứng kiến “một thay đổi tác động bốn cơ hội” ở demo synthetic, nhưng phần sản phẩm thật không thể hiện trải nghiệm tương tự. [CODE-REPORT]

**Ưu tiên đề xuất:**

1. **Truth trước:** không đánh đồng encoded check, self-report, verification và actual eligibility; kiểm tra dữ liệu nguồn và thời hạn; đảm bảo hành động không tuyên bố unlock quá mức.
2. **Real utility sau:** xây dựng một đường đi end-to-end dùng cơ hội thật, có điều kiện đủ căn cứ và action thực sự giúp tiến triển; giải quyết vấn đề action trùng tên về mặt giao diện mà vẫn giữ scope đúng.
3. **Core UX tiếp theo:** thiết kế flow `Door → requirement → next move → scenario → plan → actual confirmation → updated Door`, để minh chứng được giá trị mà onboarding đang hứa.
4. **Visual polish cuối:** ưu tiên typography hierarchy, desktop density, responsive, accessibility và interaction, không thay brand đã có.

### Phân định những gì **không** phải lỗi

- Bốn `Reserve the full required programme period` **không phải DB duplicate**: có 4 `OpportunityRuleTarget` riêng. Cần giữ chống leakage giữa OECD, World Bank Treasury, ESA, CERN. [CODE-REPORT]
- Không có real `OPEN` **không tự động là bug evaluator**: deadline TBA/manual check làm state bảo thủ theo thiết kế. Đây là **content/product coverage problem**, không nên “fix” bằng cách ép `PASS`. [CODE-REPORT]
- Pareto với bốn hành động có vector bằng nhau nhưng thuộc bốn scope khác nhau **không sai về toán học**; vấn đề là danh sách không phân biệt đủ, mục tiêu tối ưu chưa phản ánh giá trị người dùng. [CODE-REPORT][PRODUCT]
- Giữ `UNKNOWN` khác `FAIL`, manual riêng, hypothetical tách actual là những ràng buộc tốt; **không hy sinh để làm demo ấn tượng**.

### Mức đánh giá

| Trụ cột | Mức hiện tại | Diễn giải |
|---|---|---|
| Product thesis | Khá rõ, **chưa validated** | Ý tưởng action planning khác discovery, chưa có user research |
| Domain architecture | Tốt trên giấy tờ | Shared deterministic engine, explicit scope/receipts, chưa tự chạy tests |
| Real content utility | **Yếu — nút thắt #1** | 15 real doors nhưng không có real OPEN; typed coverage hẹp |
| Recommendation usefulness | **Yếu — nút thắt #2** | 4 availability actions, estimate cố định, impact ít phân hóa |
| Trust semantics | Có nền tảng, còn khoảng trống | Source-linked không tương đương verified evidence; deadline/freshness |
| Core user flow | Một phần | Link detail → action chưa strict; remaining gaps chưa đủ |
| Onboarding storytelling | Tiến bộ | IELTS demo dễ hiểu, khác biệt với behavior của real pack |
| UI system | Nhất quán | Editorial Atlas rõ; desktop/profile và density cần chỉnh |
| Data safety / local-first | Cần kiểm chứng trước release | Bootstrap inherited Firebase/network; JVM DB ở temp path |

Không nên gộp thành một điểm trung bình “7/10”: điểm nghẽn product utility phải được xử lý trước khi UI score có nhiều ý nghĩa.

---

## 1. Hiểu đúng sản phẩm và đối tượng

### 1.1 Product thesis được tài liệu hỗ trợ

- **Primary persona:** sinh viên năm 2–3, thiên về IT ở bản mô tả gốc, tìm cơ hội học tập/thực tập/cuộc thi trong giới hạn thời gian, ngân sách và năng lực. [CODE-REPORT]
- **Job to be done:** “Tôi đang ở đâu so với những cơ hội đáng theo đuổi, điều kiện nào cần bổ sung/xác minh, và bước tiếp theo nào mang lại tiến triển đáng tin với nguồn lực của tôi?” [PRODUCT, bám product promise]
- **Khác biệt dự kiến:** explainable rule checks + constrained frontier + cross-opportunity marginal gain, **không** admissions prediction, AI coach hay feed vô hạn. [CODE-REPORT]
- **Contest P0:** Android + JVM Desktop, local/offline core, curated pack, deterministic engine, one what-if scenario, one active plan. [CODE-REPORT]

### 1.2 Định vị đề xuất để thống nhất product/UI

**Đề xuất provisional:** *Porta is an opportunity-to-action planner for students.*

- **Discover** là cửa vào, không phải điểm khác biệt cuối.
- **Eligibility check** là nền tảng niềm tin.
- **Action planning & evidenced progress** là core value và điểm khác biệt.

Không mở rộng định vị thành “nền tảng phát triển bản thân tổng quát” trong vòng này: phạm vi sẽ vượt quá tính năng/dữ liệu và làm yếu demo contest. Đây là khuyến nghị chiến lược **chưa được chủ sản phẩm xác nhận**.

**Câu product promise an toàn hơn:** “Find the next meaningful step toward the opportunities you care about, with source-linked requirements and clearly labelled assumptions.”

Tránh: “Unlock scholarships”, “Verified progress” không định nghĩa, “You are eligible” từ partial rules, “Best next action” khi estimates chưa vững.

### 1.3 North-star interaction

```text
User saves a real opportunity
  → sees EXACT requirement / source / confidence boundary
  → chooses a legitimate next move
  → previews WHICH rules could change and WHICH remain unknown
  → adds it to a local plan
  → completes and explicitly confirms an actual fact/assertion
  → sees relevant opportunity receipts updated
```

Hiện các module phần lớn đã tồn tại, nhưng trải nghiệm cross-screen chưa chặt chẽ và dữ liệu thực chưa nâng đỡ được một demo mạnh. [CODE-REPORT]

---

## 2. Bản đồ thực trạng — dữ liệu và logic

### 2.1 Sự khác biệt giữa demo và real catalog

| Khía cạnh | Onboarding synthetic | Bundled real pack |
|---|---|---|
| Cơ hội | 5 fictional; 4 affected | 15 real sourced records |
| IELTS | 6.5 → 7.0 có 4 rule changes | IELTS **không** được real pack dùng ở typed rule |
| Tác động một action lên nhiều doors | Rõ ràng | Giới hạn; nổi bật là scoped availability mỗi action 1 door |
| OPEN | Có trong sample | Không real record nào có thể đạt OPEN với rule/deadline snapshot |
| Implementation | Bespoke onboarding demo (EligibilityEvaluator) | Frontier + ScenarioEvaluator + curated pack |
| Kết luận UX | Dễ cảm nhận magic moment | Ít năng lực chứng minh đúng magic moment |

**Hệ quả:** marketing/demo có thể vẫn dùng fictional để giới thiệu khái niệm, nhưng cần một **real corpus success path** với claim khiêm tốn, không dùng demo synthetic làm bằng chứng về năng lực trên data thật.

### 2.2 Real pack: số liệu nền

Theo context snapshot: 15 records; 7 scholarships, 4 internships, 2 academic programmes, 2 student contests; 23 atomic + 22 manual leaves; 18 action templates; 4 known deadlines, 9 TBA, 2 missing/rolling; 1 archived record; checked 21/10/2026. [CODE-REPORT]

Các miền typed real đang dùng chủ yếu: `AGE`, `ENROLLED`, `DEGREE_COMPLETED`, `EXPERIENCE_MONTHS`, scoped `AVAILABLE_FULL_PERIOD`. Các field `GPA`, `IELTS_SCORE`, `FIELD_OF_STUDY`, `LEADERSHIP`, `COMMUNITY_PROJECT` chưa mang lại rule-based utility tương ứng trong real pack. [CODE-REPORT]

**Không được giải quyết bằng cách tự viết ngưỡng IELTS/GPA không có trong nguồn.** Chỉ mã hóa typed condition khi câu chữ nguồn chính thức đủ cụ thể, áp dụng đúng cycle/context, có provenance và reviewer xác nhận. Khi điều kiện phức tạp hoặc chương trình-specific, giữ manual.

### 2.3 Giải phẫu bốn availability actions

| Programme | Target scope | Estimate hiện tại | Hiệu ứng |
|---|---|---|---|
| OECD | Rule-scoped | 14 ngày, LOW, FREE | 1 opportunity |
| World Bank Treasury | Rule-scoped | 14 ngày, LOW, FREE | 1 opportunity |
| ESA | Rule-scoped | 14 ngày, LOW, FREE | 1 opportunity |
| CERN openlab | Rule-scoped | 14 ngày, LOW, FREE | 1 opportunity |

`14 days` là **Porta planning lead-time assumption**, không phải duration chương trình và không được provider cung cấp. Bốn scoped target cố ý không deduplicate. [CODE-REPORT]

**Sửa đúng:**

1. Trên UI, gắn programme vào nhãn: `Confirm availability for CERN openlab` thay vì một title cho cả bốn.
2. Label rõ `Estimated time to confirm: 14 days — Porta assumption`, hoặc dùng `Time estimate unavailable` nếu không có căn cứ hợp lý.
3. Biểu diễn mục tiêu là **xác nhận** khả năng tham dự, không giả định rằng dành 14 ngày là “hoàn thành full programme period”.
4. Trên Explore/Frontier, có thể **group các action cùng loại để đỡ lặp**, nhưng từng rule-scoped assertion vẫn riêng biệt; không ghi một confirmation chung thành bốn PASS.
5. Nếu bốn opportunities đòi hỏi các thời điểm khác nhau, thể hiện rõ ngày/khoảng thời gian khi nguồn có, không gộp semantic truth.

### 2.4 Những điểm kỹ thuật ảnh hưởng trực tiếp product truth

| Hiện trạng được report | Tác động UX/product | Đề xuất |
|---|---|---|
| Deadline TBA/missing vẫn `feasible but unverified` | Đề xuất có vẻ sẵn sàng dù không biết window | Hiển thị `Timing not confirmed`; không dùng ngôn ngữ “apply now” |
| `OPEN` đòi root pass + known active deadline | Real pack không tới OPEN | Hiển thị riêng trạng thái **rule checks** và **deadline certainty**, không ép OPEN |
| `UNKNOWN` chưa tạo profile-target action (chỉ từ FAIL) | Người thiếu dữ liệu không được gợi ý điền/xác minh | Thêm `Verify/add information` như một action loại khác; không giả lập đó là improvement |
| `HAS_GAPS` có thể còn immutable FAIL | Action giải được 1 gap nhưng door vẫn không khả thi | Luôn tính `remaining blocking rules`; không nói “closer to eligible” vô điều kiện |
| Experience 720/1080 ngày; UI horizon tối đa 365 | Action không xuất hiện dù có thể quan trọng | Phân biệt `Outside your horizon` với `Not actionable`, có đường xem long-term |
| Deadline và tuổi dùng today snapshot, không reference date đủ chi tiết | Sai/trễ tại mốc kỳ tuyển | Thêm ref-date/time semantics khi nguồn yêu cầu; kiểm tra date boundary |
| `archived` không tham gia evaluation | Cơ hội lưu trữ có thể hiển thị sai state | Định nghĩa archived/cycle state độc lập với dated CLOSE |
| Scoped fingerprint chứa source wording | Thay đổi copy có thể làm assertion stale | Xem xét fingerprint canonical semantics / migration, không tự động chuyển PASS cũ |
| User evidence metadata không đổi evaluator outcome | “Evidence-backed/verified” dễ bị hiểu quá mạnh | Chuẩn hóa provenance language; chỉ nói self-declared/document provided |

### 2.5 Điều chỉnh outcome taxonomy, **không cần phá engine hiện có ngay**

Thay vì ép năm `EligibilityState` làm toàn bộ công việc diễn giải, UX có thể trình bày **hai lớp**:

- **Requirement assessment:** `Known checks passed`, `Actionable gaps`, `Information needed`, `Not actionable under current rules`.
- **Application timing:** `Confirmed deadline`, `Deadline unverified`, `Closed for this cycle`, `Archived/older cycle`.

Từ kết quả evaluator hiện có, hiển thị “Known checks passed, deadline needs confirmation” rõ hơn `Needs verification` đơn lẻ. Vẫn giữ overall internal `OPEN/HAS_GAPS/NEEDS_INFO/NOT_ACTIONABLE/CLOSED` cho ổn định, sau đó cân nhắc refactor domain riêng nếu cần.

**Bất biến bắt buộc:** atomic PASS chỉ có nghĩa *provided comparable value matches encoded condition*; không phải eligibility của provider, evidence authenticity hay chance of selection.

---

## 3. UX / information architecture: audit từng flow

### 3.1 Onboarding (Android & Desktop screenshots)

**Điểm mạnh:** Editorial Atlas (Newsreader + Manrope, dark + lavender) có bản sắc; headline/copy sau chỉnh sửa ngắn hơn; synthetic demo minh họa cause-and-effect; Step 4 tách sample/real. [SCREEN]

**Vấn đề cần sửa:**

- Kể câu chuyện IELTS mở bốn “doors” nhưng app real chủ yếu đề xuất availability rule-scoped. **Story-product mismatch** là vấn đề nội dung/định vị, không phải animation.
- Demo Step 1 chọn Scholarships/Internships/Exchange chỉ thay thứ tự fictional cards, không tạo personalization thực. Copy cần nói chính xác tác dụng; hoặc đơn giản bỏ bước chọn nếu không tạo giá trị thật.
- Step 2 số/slider/status đã tách rõ 6.5→7.0; nhưng impact còn là 4 dấu chấm và text, ít mô tả rõ **before/after by rule**. [SCREEN]
- Step 3 bốn arch cards có visual identity nhưng lặp lại cùng status; bản sắc hình vòm nên là nơi biểu diễn thay đổi có ý nghĩa, không phải bốn khung giống nhau.
- Desktop onboarding content ~hẹp nhưng CTA gần full-window width, cho thấy footer không dùng cùng container alignment. [SCREEN]
- Cần runtime kiểm tra scroll reset, 320×640, font scale 200%, keyboard navigation/reduced motion: screenshot không đủ chứng minh đã fix. [VERIFY]

**Đề xuất:** giữ 4 bước nhưng Step 2–3 hiển thị rõ `IELTS check only`; một bảng before/after và expand “why”. Cân nhắc nối demo với một real example sau CTA thay vì coi sample synthetic là toàn bộ proof. Tập trung semantic animation khi delta thay đổi, không thêm particle/confetti.

### 3.2 Doors list

**Điểm tốt:** đếm theo trạng thái, saved và filter, compact cards, status tách rõ. [SCREEN]

**Vấn đề:**

- Title `Your doors` nhưng sample mode ghi `SAMPLE PROFILE`; đoạn “your current profile” gây mâu thuẫn.
- 7 filter chips dài; trên mobile có thể cuộn nhiều, ưu tiên nhóm chính và Advanced filters.
- Card cho biết condition snippet nhưng chưa làm rõ **exact gap & next action**; chưa có tìm kiếm theo tên hoặc category filter. [CODE-REPORT][SCREEN]
- 15-source banner không giải thích freshness/checked date từng record; unknown deadline cần mốc xác minh.
- `Current checks passed` có thể bị hiểu là eligibility pass toàn diện.

**Đề xuất UI:** list card gồm (1) title/category/organizer, (2) trạng thái cùng lý do ngắn, (3) deadline confidence, (4) explicit `View requirements`; saved icon đủ hitbox ≥48dp. Sample mode CTA `Create profile for your own checks`.

**Desktop:** consider `search/list + detail pane`, không kéo những card ít thông tin hết chiều ngang màn hình. Chỉ dùng master-detail khi khối detail có đầy đủ receipt và sources.

### 3.3 Opportunity detail — **điểm nối đang thiếu**

Theo source report, detail có receipt/source metadata/soft criteria/action shortcut nhưng URL được render như text; action shortcut chọn **first linked template** chứ chưa đảm bảo action phù hợp với unmet rule, constraint/ranking. [CODE-REPORT]

**Phải chuyển thành cấu trúc:**

1. `What this is` — tổ chức, loại, cycle, deadline và nguồn chính thức click được.
2. `Where you stand` — known checks / unknown/manual / unmet; hard vs advisory rõ ràng.
3. `What you can do` — chỉ hiển thị **eligible candidate** từ Frontier theo đúng `(opportunityId, ruleId)`, nếu không có thì nói `No verified action yet; verify requirement`.
4. `Preview impact` — delta của rule này và cross-opportunity tác động nếu có.
5. `Remaining requirements` — những điều kiện còn lại sau action.

Không có shortcut “Preview action” nếu nó dẫn tới template không giải quyết gap đang nêu.

### 3.4 Next moves / Frontier — **nút thắt UX chính**

Screenshot desktop thể hiện panel `Your limits` rộng, chart 1 bubble chồng (`4`) và 4 action trùng title; selected move chỉ tác động 1 door. [SCREEN]

**Vấn đề gốc:** presentation đang bộc lộ cấu trúc calculation hơn là giúp quyết định. Không thể khắc phục chỉ bằng đổi tên `Action Frontier` thành `Next moves`.

**Thứ bậc đề xuất:**

1. Header `Your next moves` với 1 câu giải thích đáng tin và cảnh báo sample profile.
2. `Recommended next step` **nếu có ứng viên đủ điều kiện xếp hạng và estimate confidence phù hợp**. Nếu không: empty/verification state minh bạch.
3. Preview outcome: *which checked requirement, which doors, remaining blockers, timing uncertain*.
4. Alternatives **thực sự khác nhau**; group equivalent family, không duplicate labels gây nhiễu.
5. Filter/limits trong collapsible side panel; Desktop có thể mở theo nhu cầu.
6. Pareto/impact plot chỉ xuất hiện khi có trade-off mang thông tin; nếu vectors trùng nhau, list so sánh có nhãn programme/scope rõ hơn.
7. Excluded actions có reasons và link điều chỉnh hạn mức, không giấu.

**Quan trọng:** Pareto là một explanation tool, không phải thước đo UX mặc định. Nếu chỉ có một unique trade-off vector, tốt hơn hiển thị một insight “4 programme-specific confirmations; 14-day assumptions; deadlines unverified”.

### 3.5 What-if, active plan, actual update

Source report xác nhận scenario memory-only; save một `PlanItem`; completion không tự sửa actual; sau đó mới explicit actual update. Đây là nền tảng đúng. [CODE-REPORT]

**Cần hiển thị rõ ba pha:**

- **What if?** — giả sử fact/assumption thay đổi.
- **Plan it** — tôi sẽ làm gì và khi nào; không có nghĩa đạt điều kiện.
- **Confirm reality** — người dùng xác nhận thông tin actual sau khi hoàn thành, provenance được ghi đúng.

Scenario screen phải có **exact rule deltas** + **remaining hard/manual/deadline issues** để ngăn effect overclaim. Với scoped availability chỉ một source programme thay đổi, nói rõ “This confirmation does not apply to other programmes”.

**One active plan** có thể là intentional focus mechanic cho MVP; nếu vậy ghi rõ `One step at a time` và bảo toàn lịch sử khi thay plan (nếu history là một phần mong muốn). Hiện `replaceForProfile` xóa plan trước; không nên diễn giải như multi-task tracking. [CODE-REPORT]

### 3.6 Profile: empty state và editing

**Empty:** hiện một card chiếm gần hết bề ngang Desktop, rất ít nội dung. Đưa về content max-width + nêu value (compare requirements, find gaps, next moves) và phân biệt sample/real. [SCREEN]

**Form:** nhóm Essentials, Academic record, Leadership, Context là đúng hướng. Nhưng có nhiều trường không ảnh hưởng real dataset; khiến người dùng nhập liệu mà không nhận được giá trị. [SCREEN][CODE-REPORT]

**Đề xuất progressive profiling:**

- Stage A: tối thiểu để bắt đầu, giải thích vì sao cần (study level + field nếu giữ required, và thông báo coverage thực tế).
- Stage B: `Improve your checks` — chỉ yêu cầu fact nếu ít nhất một selected/saved opportunity có rule sử dụng hoặc sẽ có source-supported lookup.
- Stage C: contextual prompts trên detail: experience/enrollment/degree/age hoặc manual review.

Không hứa “More accurate matches” từ GPA/IELTS nếu real rules chưa dùng chúng. Form phải đánh dấu required, validate type/range, dẫn đến lỗi cụ thể khi Save disabled, loại bỏ placeholder-only labels, giải thích role of evidence references, phân biệt không biết và không đáp ứng.

**Desktop:** constrain form width/cột hợp lý; trường text dài không chiếm 1.300 px nếu không có lý do; tăng focus state/keyboard semantics.

### 3.7 Writing and terminology recommendations

| Current | Recommended / condition |
|---|---|
| `Your doors` (sample) | `Explore opportunities` + prominent `Sample profile` |
| `15 sourced doors` | `15 opportunities with source references · Last checked Oct 21` (nếu record dates phù hợp) |
| `Current checks passed` | `Known checks passed` hoặc `Assessed rules passed`, kèm disclaimer |
| `Needs action` | Giữ, nhưng hiển thị **exact failed requirement** |
| `Needs verification` | Giữ, hiển thị “Which information or manual check?” |
| `Reserve the full required programme period` | `Confirm full-period availability for [Programme]` nếu đúng nghĩa template |
| `14 days` | `Estimated confirmation lead time: 14 days (Porta assumption)` nếu giữ |
| `Verified impact` | `Encoded rule changes` hoặc `Checks potentially improved` tùy actual data |
| `Actual profile: Not confirmed / What-if: Yes` | `Current: Not confirmed / Scenario: Assume available` |
| `Why it ranks` | `Why this step is suggested` + explain saved/impact/time/cost |
| `4 hard gaps resolved` | `4 requirements would change in this scenario` (không đánh đồng verified) |
| `Your source of truth` | `Your current details` |
| `1 doors` | `1 opportunity` (pluralization fix) |

**Không chuẩn hóa mọi thứ thành copy quá đơn giản làm mất chuyên môn.** Giữ `Pareto frontier` trong advanced explanation, nhưng user-facing primary title nên là `Compare trade-offs` hoặc `Your next moves`.

---

## 4. Recommendation engine — chính xác, hữu ích, giải thích được

### 4.1 Chẩn đoán engine

Theo report, candidates được tạo từ referenced template; profile-target thường cần FAIL; scoped-target cho FAIL hoặc UNKNOWN; dedup theo fact/value hoặc opportunity/rule/value; chọn template ngắn nhất trước; simulate trên tất cả doors; filter horizon/effort/cost/deadline; Pareto dominance qua counts (newly-open/improved, saved subsets, duration, effort, cost); rank lexicographic. [CODE-REPORT]

Đây là **deterministic constrained optimizer hợp lệ theo objective hiện tại**, nhưng objective **chưa đủ giống mục tiêu thực tế của sinh viên**:

- Đếm mỗi `improved door` bằng nhau dù còn nhiều manual blockers.
- Missing source deadline không làm giảm eligibility của candidate (chỉ timing unverified).
- Không đánh giá confidence của duration/cost/effort.
- Cross-set diversity không tính: 2 action khác affected sets có thể dominate bằng count dù user rất quan tâm door khác.
- Saved preference có thể thay cả Pareto membership, không chỉ reorder; UI cần giải thích.
- Pre-dedup chọn duration ngắn nhất có thể làm mất alternate trade-off.

### 4.2 Bốn thay đổi ưu tiên

**R1 — Action semantics & provenance:** Mỗi action có `kind = VERIFY | IMPROVE | PREPARE`, scope, source/assumption metadata, method to confirm, and measurable completion criterion. Chỉ một số action được phép mang `candidate fact delta`; các hành động kiểu plan/prepare không tự chuyển sang PASS.

**R2 — Explanations trước complexity:** Với candidate, tạo `ImpactExplanation`: base and simulated receipt per affected door; changed rule IDs; remaining FAIL/UNKNOWN/REVIEW; deadline certainty; why ranked; which estimates assumed. UI consumes object này thay vì ghép string tùy tiện.

**R3 — Dedupe vs grouping:** Giữ global fact action one-to-many; giữ scoped assertion riêng; nhưng group cùng `actionFamily` trong UI, mỗi scoped option là subitem có tên programme. Tránh gộp truth. Thử scenarios khác nhau và bảo toàn Pareto alternatives nếu effort/cost trade-off khác nhau.

**R4 — Recommendation fallback:** Khi không có measurable genuine unlock, thay “best next move” bằng các bước có giá trị thật như `Verify deadline`, `Complete missing profile fact`, `Read official eligibility rules`, được **phân loại verification tasks**, không tính như unlock/progress một cách gian dối. Nếu không có next step uy tín, nói thẳng `We cannot recommend a reliable action yet`.

### 4.3 Tránh “sửa engine theo cảm giác”

Cần benchmark bằng 6 test fixtures:

1. 4 doors sharing **one** IELTS global rule: one action, 4 changed rules (synthetic fixture).
2. 4 scoped availability confirmations: 4 independent truth targets; UI grouped nhưng single confirmation only one scoped PASS.
3. Known immutable FAIL + actionable FAIL: sau action overall `NOT_ACTIONABLE`; UI không gọi door unlocked.
4. Missing profile fact only UNKNOWN: verification action, không chuyển PASS trước user confirmation.
5. TBA deadline: impact annotated `timing unverified`; known past deadline excluded.
6. Two non-dominated action alternatives with different effort/duration/affected sets: preserve both and explain trade-offs.

### 4.4 Pareto score và outcome integrity

Không nên vội thêm một AI score/phần trăm matching. Nếu phát triển evaluator, ưu tiên các objective có thể giải thích: unique affected conditions, count by progress class, saved opportunity relevance, deadline confidence, feasibility confidence, effort/cost bands. Nếu thêm trọng số, document rõ vì sao, không biến nó thành “success probability”.

---

## 5. Data/source strategy: 15 opportunities đủ hay chưa?

**Số lượng không phải vấn đề chính.** 15 records có source nhưng coverage cốt lõi chưa tạo đủ trường hợp real-data meaningful. [CODE-REPORT]

### 5.1 Curated pack quality bar

Cho mỗi opportunity, tối thiểu:

- Official URL **clickable**, source note, owner/publisher, cycle/applicability.
- `lastCheckedOn` per record, evidence capture method/manual review status.
- Deadline state rõ `Known/TBA/Rolling/Missing` + source và checked date; app không suy đoán hạn nộp.
- Rule semantics (typed/manual), evidence basis, scope; nêu rõ `manual review required`.
- Chỉ có action template khi target là controllable/time-dependent, completion có thể định nghĩa, estimate là sourced hoặc assumption ghi rõ.
- Điều kiện `UNKNOWN` không tự thành `FAIL`; untrusted custom pack không hưởng cùng nhãn verified như official-source checked content.

### 5.2 Vertical slice đề xuất

**Không bịa thêm rule cho Chevening, Erasmus hay bất kỳ provider nào.** Chọn 3–5 opportunities trong catalog **sau khi đọc lại nguồn chính thức của đúng cycle** để có thể:

1. Xác định ít nhất một điều kiện typed đáng tin trên từng opportunity.
2. Có ít nhất một action hợp pháp và hữu ích cho người dùng.
3. Có ít nhất một ví dụ **một fact cải thiện nhiều opportunity checks** hoặc giải thích được vì sao không thể gộp.
4. Có ít nhất một path thể hiện deadline/unknown mà không hứa eligibility.
5. Có exact remaining gaps cho từng scenario.

Nếu không tìm được 3–5 cases này trong 15 records, **thay đổi selection của pack**, không hạ tiêu chuẩn truth. Có thể giữ các học bổng nổi tiếng ở mục Explore nhưng không nhất thiết dùng chúng làm engine demo.

### 5.3 Pack lifecycle

- Pack import hiện activate ngay, không preview diff; bundled không auto-upgrade một số active pack cũ. [CODE-REPORT]
- Tạo cơ chế **pack version visibility + changes detected + user-approved update**, nhất là nếu rule fingerprints thay đổi.
- Archive/closed cycle phải rõ, tránh ép deadline known cũ đại diện cho “archived”.
- Bảo toàn saved/plan/assertion khi update hoặc báo rõ cái nào stale; không silently mark stale facts PASS.
- Chưa cần crawler, registry hay cloud sync để có MVP đáng tin; một curated release process có review đã là cải tiến lớn.

---

## 6. Platform quality, privacy và kỹ thuật

### 6.1 Những việc release-blocking / data integrity

**T1. Local-first claim vs inherited bootstrap.** `AppInitializer.kt` còn Remote Config/analytics/notifications/anonymous auth startup path. Đừng tuyên bố absolute “zero network / no Firebase” trước khi chạy cold-start network trace trên Android/Desktop, đặc biệt khi chưa có config. Nếu không cần: gate/remove inherited initializer side effects khỏi PORTA build, rồi test lại. [CODE-REPORT][VERIFY]

**T2. Desktop database persistence.** JVM Room path dưới `java.io.tmpdir/io.github.vietdsk6.porta`; OS có thể xóa. Chuyển sang app-data directory ổn định theo OS, kiểm tra migrate/restore, data remains after restart. [CODE-REPORT]

**T3. “Clear my data” không xóa toàn bộ app state.** Hiện clear flow không đảm bảo xóa saved opportunities, active pack, DataStore; UX phải tách `Clear profile` và `Reset all local app data` với phạm vi mô tả chính xác. [CODE-REPORT]

**T4. Imported action IDs uniqueness.** Validator chưa enforce global uniqueness; `firstOrNull` scenario lookup có ambiguity risk. Enforce unique IDs toàn pack hoặc namespace theo opportunity trước khi accept; atomic import không được làm mất active pack. [CODE-REPORT]

**T5. Time & rollover.** Current date không là reactive Flow. Re-evaluate khi app resume, local-date rollover, cycle/pack update; test deadline-inclusive day semantics. [CODE-REPORT]

### 6.2 QA và accessibility

Report tìm 153 `@Test` annotations, nhưng **không chạy bất kỳ test nào** trong lần code audit. Do đó không thể tuyên bố build/test pass trong bản này. [CODE-REPORT]

Cần thêm test cho: operator chưa được cover, ScenarioEvaluator standalone, no-OPEN interpretation, imported duplicate action IDs, update pack existing, source freshness/archived states, plan actual confirmation not auto-pass, unknown verification, real-data example; Android 320×640/font 200% và Desktop keyboard focus/tooltip/screen reader/reduced motion.

Approved commands theo report: `spotlessCheck`, `:shared:jvmTest :shared:testAndroidHostTest`, `:androidApp:assembleDebug`. Hãy chạy thật trên repo và đính kèm log/commit hash vào kết quả; không assume đều pass.

### 6.3 Responsive design rules đề xuất

- Duy trì theme Editorial Atlas, không chuyển sang dashboard generic.
- `<840dp`: bottom nav và 1 cột; `≥840dp`: rail, nhưng rail cần width/labels/hitbox rõ. Frontier `≥900dp` master-detail có thể giữ.
- Desktop Doors nên có list/detail khi đủ detail content; profile edit max-width hoặc grid cột hợp lý; onboarding CTA căn theo content width.
- Không dùng full-width single-line text input nếu chiều dài thông tin không tương xứng.
- Reduced motion, keyboard navigation, focus order, selected semantics, screen-reader announcement khi result thay đổi.
- Ưu tiên `content hierarchy` và interactive states trước animation thêm.

---

## 7. Roadmap ưu tiên và tiêu chí nghiệm thu

### Quy tắc xếp ưu tiên

- **P0 — Blocking:** sai lệch claim dữ liệu, người dùng hiểu sai eligibility, dữ liệu cá nhân/persistence, recommendation misleading, critical path không hoạt động.
- **P1 — Core product:** real utility + journey + explanations.
- **P2 — Craft:** responsive, design polish, user research, optional value.

| ID | Ưu tiên | Vấn đề | Hướng giải quyết | Acceptance (rút gọn) |
|---|---|---|---|---|
| P0-01 | P0 | Semantic overclaim | Audit copy/state `verified/open/eligible/resolved` | Atomic PASS không đồng nghĩa admission; scenario không mutate actual; remaining blockers hiện rõ |
| P0-02 | P0 | Availability 14-day ambiguity | Programme-specific name, estimate provenance, confirmation semantics | Không còn 4 dòng khó phân biệt; 14d rõ là assumption, không phải duration chương trình |
| P0-03 | P0 | Real pack không có proof path | Second source review + 3–5 real vertical-slice cases | Ít nhất 3 real scenarios có meaningful, truthful rule deltas; không invent provider rules |
| P0-04 | P0 | Wrong detail→action | Bind shortcut to unmet rule + scenario eligibility | Không navigate action irrelevant; rule receipt ↔ template chính xác |
| P0-05 | P0 | Local-first/desktop data risk | Runtime bootstrap audit; durable DB path | Cold launch network behavior documented; profile/pack/plan survive desktop restart |
| P0-06 | P0 | Import ambiguity | Unique action IDs, atomic validation | Duplicate import rejected without active-pack loss |
| P1-01 | P1 | Missing contextual next steps | Verification action type vs improvement type | UNKNOWN prompts check/fact input, không tính fake unlock |
| P1-02 | P1 | Next moves hierarchy | Recommendation-first / grouped alternatives / optional plot | 1st viewport trả lời what/why/impact; plot khi có real trade-off |
| P1-03 | P1 | What-if lacks remaining gaps | Receipt-level diff + unresolved summary | User thấy changed + unchanged blockers, deadline uncertainty |
| P1-04 | P1 | Profile asks unused data | Progressive profiling linked to selected opportunities | Relevant fields requested on demand; optional remains unknown |
| P1-05 | P1 | Pack lifecycle | Visible version/freshness/upgrade handling | No silent source assumptions; stale assertion safely handled |
| P1-06 | P1 | Saved priority opacity | Explain ranking & dominance, clarify toggle | UI nói rõ saved may change frontier membership |
| P1-07 | P1 | Midnight/archived state | Clock refresh + cycle state | Date/cycle changes correctly reflected |
| P1-08 | P1 | Data reset incomplete | Separate scope-specific clear/reset | User sees exact data removed and can verify result |
| P2-01 | P2 | Desktop UI density | Responsive max width, list/detail, consistent CTA | Desktop screenshots no unexplained full-width form/footer |
| P2-02 | P2 | UI visual storytelling | Meaningful state transitions in sample and real preview | Delta shown with semantic cues, reduced motion respected |
| P2-03 | P2 | Discovery ergonomics | Search/category/deadline filters/empty states | Search & filters tested on mobile/desktop |
| P2-04 | P2 | Usage validation | 5–8 moderated user sessions with realistic tasks | Measure comprehension/time-to-insight/confusion, record notes |

### Recommended execution sequence (dependency-first, not calendar estimate)

**Wave A — Make claims correct and data safe.** P0-01, P0-02, P0-05, P0-06; tests and design wording guardrails. Đầu ra: source/cycle/actual/hypothetical semantics không sai.

**Wave B — Make real engine useful.** P0-03, P0-04, P1-01, P1-03; 3–5 checked real scenarios. Đầu ra: người dùng mở một real door và thấy một bước tiếp theo có căn cứ.

**Wave C — Show value fast.** P1-02, P1-04, P1-06, P2-01, P2-02; unify Android/Desktop journey. Đầu ra: first-use demo và real-use flow kể cùng một product story.

**Wave D — Stabilize submission & learn.** P1-05, P1-07, P1-08, P2-03, P2-04; regression, accessibility, evidence, screenshots/GIF, contest materials. Đầu ra: chất lượng có thể kiểm chứng, không chỉ cảm giác polished.

---

## 8. Product acceptance scenarios dùng để kiểm tra cuối

### Scenario A — Synthetic magic moment

Input fictional IELTS 6.5; user changes to 7.0. Exactly four fictional **IELTS checks** change; fifth sample door already passes at 6.0 remains unchanged. App không nói “4 scholarships awarded” hay update real profile. Reverse/replay deterministic. [CODE-REPORT]

### Scenario B — Programme scope integrity

CERN scoped availability UNKNOWN; simulate yes. Chỉ CERN rule có thể chuyển PASS; OECD/ESA/WB không tự chuyển. Actual profile remains unchanged until explicit update. 14d annotated planning assumption if displayed. [CODE-REPORT][PRODUCT]

### Scenario C — Unknown vs failed

No experience months provided ⇒ `UNKNOWN`, not `FAIL`; UI yêu cầu add/verify info, không recommend “gain 36 months” như thể người dùng chắc chắn có 0. [PRODUCT]

### Scenario D — Partial improvement

Mixed root includes age immutable FAIL + degree actionable FAIL. Simulated degree completion may improve one receipt, but overall remains ineligible/non-actionable; UI must show remaining immutable blocker and avoid “door unlocked”. [CODE-REPORT][PRODUCT]

### Scenario E — Deadline/trust

Known passed deadline ⇒ CLOSED regardless of checks. TBA/missing ⇒ timing-unverified, không coi chắc chắn application open. Official link and last-checked visible. [CODE-REPORT][PRODUCT]

### Scenario F — Save/recommendation explainability

Save one opportunity and enable prioritization. If Pareto membership changes, UI explains why the preferred result changed; toggle off reproducibly restores candidate ordering for same snapshot. [CODE-REPORT][PRODUCT]

### Scenario G — Full confirmed progress loop

Plan stored with actual profile; user marks complete; **no eligibility changes yet**. Only separate actual update changes fact/assertion; evaluation recomputes; affected door shows changed receipt + provenance label appropriate. [CODE-REPORT]

### Scenario H — Desktop privacy/persistence

After fresh install, cold-launch network inspected; profile/pack/plan saved, app closed and relaunched, data survives from durable user dir; reset scope behavior matches UI. [VERIFY]

---

## 9. Product metrics: đo điều gì sau khi sửa

Không có interviews, analytics hay real validation trong repository report, nên các số liệu dưới đây là **đề xuất đo**, không phải kết quả đã đạt.

| Metric | Định nghĩa đề xuất | Tại sao cần |
|---|---|---|
| Time to first trustworthy insight | Từ first open đến lúc thấy một exact rule & source phù hợp | Xác định discovery có chuyển thành hiểu biết |
| Gap comprehension rate | Người test nói lại đúng condition thiếu/unknown/manual | Đo clarity, tránh false confidence |
| Action differentiation rate | Phân biệt được vì sao chọn action A thay B | Đánh giá recommendation, không chỉ click |
| Preview comprehension | Hiểu affected rules vs remaining blockers và hypothetical vs actual | Đánh giá trust |
| Real-data useful scenario coverage | Số real doors có source-supported actionable delta và explanation | Đo coverage thay vì chỉ catalog size |
| Profile field utility | % trường profile được ít nhất một real rule sử dụng | Tránh form vô ích |
| Plan → confirmed actual | Tỉ lệ plan hoàn thành có actual confirmation | Đo core loop, không phải admissions success |
| Source freshness coverage | % record được review trong window do đội định nghĩa | Đo trust operational |
| Cross-platform parity | Same input+pack+clock → same receipts/frontier | Chứng minh KMP value |

Trước contest, có thể dùng moderated task runs và scripted fixtures thay analytics network, phù hợp local-first intent.

---

## 10. Những quyết định chỉ product owner có thể chốt

Để không làm kẹt triển khai, báo cáo dùng giả định **action planning là khác biệt cốt lõi, Android+Desktop contest MVP, trust-first**. Cần xác nhận cuối cùng:

1. **Positioning:** action planning là core hay chỉ là tính năng phụ của scholarship discovery?
2. **Primary audience:** IT students vs sinh viên đa ngành; phạm vi địa lý/language ra sao?
3. **Real corpus success bar:** có chấp nhận không real door OPEN nếu có partial verified-rule progress rõ ràng, hay muốn một real complete-check scenario?
4. **“Verified” vocabulary:** nghĩa encoded-source check hay độc lập xác minh evidence? Khuyến nghị không dùng unqualified.
5. **One active plan:** intentional focus mechanic hay technical MVP compromise? Có cần lịch sử không?
6. **Durations/effort/cost:** chấp nhận uncertainty/unknown hay vẫn muốn luôn gán fixed estimate? Khuyến nghị unknown khi thiếu căn cứ.
7. **Zero-network/no Firebase:** điều kiện contest tuyệt đối hay chỉ nói core eligibility offline?
8. **Catalog stewardship:** ai kiểm tra nguồn, chu kỳ cập nhật và sign-off?
9. **Success metric:** demo trong 30 giây, rule comprehension, action selection hay confirmed improvement?
10. **Contest-vs-long-term:** giới hạn thời gian cho reliability trước khi thêm features?

Không cần trả lời toàn bộ mới bắt đầu Wave A. Các câu 1, 3, 4, 6 và 7 ảnh hưởng trực tiếp đến architecture/content và nên chốt trước Wave B.

---

## 11. Traceability — nơi Codex cần kiểm tra trực tiếp

Các đường dẫn dưới đây là **đường dẫn tương đối trong repository được context report liệt kê**, không phải file mà reviewer đã đọc trực tiếp.

| Topic | Paths / source |
|---|---|
| Product promise | `PORTA_PRD.md`, `README.md`, `CONTEST_SCORECARD.md` |
| Source rules | `OPPORTUNITY_PACK_SOURCES.md`, `data/source/local/pack/PortaContestPackData.kt` |
| Eligibility | `domain/model/Opportunity.kt`, `domain/usecase/EligibilityEvaluator.kt`, `domain/model/RuleFingerprint.kt` |
| Frontier | `domain/model/ActionFrontier.kt`, `domain/usecase/ActionFrontierEngine.kt` |
| What-if | `domain/usecase/ScenarioEvaluator.kt`, `presentation/screens/scenariopreview/` |
| Doors/detail | `presentation/screens/doors/`, `presentation/screens/opportunitydetail/` |
| Frontend Frontier | `presentation/screens/frontier/`, `presentation/screens/activeplan/`, `presentation/screens/actualprofileupdate/` |
| Profile | `presentation/screens/profilesetup/`, `presentation/screens/studentprofile/`, `data/repository/ProfileRepository.kt` |
| Persistence | `data/source/local/AppDatabase.kt`, platform-specific `DatabaseProviderImpl` |
| Bootstrap | `root/AppInitializer.kt`, `root/Di.kt`, `root/AppConfiguration.kt` |
| Packs | `OpportunityPackParser.kt`, `OpportunityPackValidator.kt`, `OpportunityPackRepository.kt` |
| UI theme | `designsystem/.../theme/Color.kt`, `Type.kt`, `Spacing.kt`, `Theme.kt` |
| Tests | `EligibilityEvaluatorTest.kt`, `ActionFrontierEngineTest.kt`, `BundledOpportunityPackProviderTest.kt`, ViewModel/UI integration tests |

**Doc drift cần dọn:** `PORTA_PRD.md` vs `../AiGuidelines/project/prd.md` có performance target khác nhau; `ui_ux.md` dùng palette/font cũ; onboarding spec cũ nêu 5 categories nhưng current UI có 3; README “six facts” lệch 2 required fields. Chọn một spec authoritative và ghi change log; không tự động sửa source behavior chỉ để khớp tài liệu cũ.

---

## 12. Exit criteria cho bản Porta cải thiện

Chỉ coi là đạt khi có **bằng chứng**, không chỉ screenshot:

- [ ] 3–5 real sourced scenarios minh họa rõ action/verification outcome, người review có thể mở source.
- [ ] Mỗi scenario có exact changed checks, remaining blockers, timing confidence, và tách hypothetical/actual.
- [ ] Action per-programme không bị leak, có tên chương trình, estimate caveat minh bạch; không còn bốn duplicate-looking rows.
- [ ] Một Door thực tế dẫn đến action hợp lệ hoặc trạng thái “no trustworthy action” có giải thích.
- [ ] Unknown không bị xem là Fail, manual không thành auto-pass; một action không đồng nghĩa guaranteed eligibility.
- [ ] Navigation/CTA/labels nhất quán Android và Desktop; Desktop không có footer full-width lệch content.
- [ ] Local-first/network claim được kiểm tra; Desktop DB dữ liệu tồn tại qua app restart.
- [ ] Pack import/action ID uniqueness/clear scope/date rollover có test hoặc bằng chứng kiểm thử thủ công.
- [ ] Build/test gates chạy thật, failure/skip được ghi rõ; accessibility checks có kết quả.
- [ ] README/PRD/claim demo khớp implementation sau thay đổi.

### Đề xuất cuối

**Không làm lại theme. Không vội tăng số lượng học bổng. Không thêm AI mentor.** Hãy làm cho một đường đi thật sự đúng và có ích: *một cơ hội đáng muốn → một điều kiện rõ → một hành động có căn cứ → một thay đổi mô phỏng trung thực → một kết quả được xác nhận*. Khi đó UI/animation mới có điều đáng giá để trình bày.

---

### Giới hạn của bản đánh giá

Báo cáo này là synthesis giữa source-analysis do Codex xuất, screenshots và phân tích sản phẩm. Nó **không** là source-code audit độc lập, không xác minh độ đúng pháp lý/yêu cầu provider theo thời gian thực và không thay thế usability test. Những khuyến nghị cần thay đổi rule phải kiểm tra lại source chính thức đúng cycle trước khi implement.
