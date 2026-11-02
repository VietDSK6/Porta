# PORTA — Kế hoạch cải thiện toàn diện UI/UX, Visual & Product Flow

> Historical AI-assisted planning artifact. Retained for provenance; see the archive README and current scorecard.

**Phiên bản:** 1.0 · **Ngày:** 28/10/2026
**Phạm vi:** Onboarding; Level 1 (Doors, Next moves, Profile); Level 2 (Opportunity details, What-if, Edit academic profile, Active plan, các trang source/pack nếu trực tiếp mở từ Level 1); Level 3 (Update actual profile và các màn drill-down mở tiếp từ Level 2). Android là ảnh tham chiếu chính; yêu cầu áp dụng tương đương cho JVM Desktop qua Compose Multiplatform.

**Nguồn cơ sở:** Screenshot Android light mode mà người dùng gửi qua các lượt gần nhất; screenshot Android/desktop và onboarding trước đó; `PORTA_PRODUCT_CONTEXT.md` (Codex static trace 24/10/2026); `PORTA_PRODUCT_AUDIT_2026-10-24.md`; `PORTA_VISUAL_UX_ART_DIRECTION.md`. Không trực tiếp chạy repository hoặc kiểm thử bản build hiện tại trong lần lập kế hoạch này. Các thông tin code cũ phải **được Codex xác nhận lại** trên working tree mới.

**Định nghĩa navigation level:** Đây là **độ sâu điều hướng**, *không* phải priority. Level 1 là ba tab chính. Level 2 là màn mở trực tiếp từ Level 1. Level 3 là màn mở tiếp từ Level 2. Onboarding là flow riêng trước Level 1.

---

## 0. Quyết định cần chốt trước khi viết code

1. **Giữ nguyên nhận diện**: light ivory/dark charcoal, lavender, serif editorial cho hero và sans cho nội dung. Không rebrand; không giảm cỡ chữ hàng loạt để tiết kiệm chỗ.
2. **Giảm lượng thông tin mặc định, không cắt căn cứ**: tầng `Glance → Understand → Evidence`; full rules/sources/assumptions vẫn truy cập được.
3. **Mỗi màn có một việc chính**: Doors = khám phá; Next moves = chọn/tiếp tục việc đáng làm; Profile = tình trạng hồ sơ; Details = điều kiện; What-if = tác động của một giả định; Active Plan = tiến độ; Actual Update = xác nhận sự thật.
4. **Không làm đẹp bằng cách nói sai**: pass một rule ≠ đủ điều kiện ứng tuyển; worked completed ≠ factual outcome confirmed; reviewed source ≠ live deadline verified; sample ≠ actual profile.
5. **Minh họa có ý nghĩa**: Door/Arch, Threshold, Path, Ripple ở một số vị trí trọng điểm; không nhét ảnh lớn vào mỗi card, không dùng stock illustration SaaS.
6. **Android và Desktop chung component/data contract**, bố cục adaptive khác nhau. Ngay trong wave UI cần test light/dark, font scale, keyboard/focus và resize.
7. **Không sửa engine chỉ để tạo happy-path**. Bất kỳ tác động, counts hoặc deadline caution trên UI phải tính từ evaluator/ScenarioEvaluator, không hardcode.
8. **Ưu tiên khắc phục sản phẩm đã có hơn bổ sung bề mặt mới**. Nâng dữ liệu opportunity và chất lượng recommendation là track song song, không che bằng animation.

## 1. Chẩn đoán tổng thể

### 1.1 Đã tốt
- Light mode thống nhất và dễ đọc hơn dark prototype cũ; palette lavender là điểm nhận diện tốt.
- Next moves chuyển thành **recommendation-first**; filter constraints thu gọn.
- User profile có tóm tắt cá nhân; progressive edit đã xuất hiện.
- What-if thể hiện actual/profile không đổi, per-rule delta, unresolved checks và deadline caveats.
- Active plan và Actual Update đã tạo vòng lặp giữa dự định, completion và xác nhận giá trị thực tế.

### 1.2 Nút thắt còn lại

| ID | Quan sát cụ thể | Tác hại | Độ ưu tiên |
|---|---|---|---|
| G01 | Hầu hết màn có nhịp `hero → paragraph → surface → paragraph → CTA` | Cảm giác đọc tài liệu, đơn điệu | P1 |
| G02 | Source notes, caution, raw rules và disclaimer lặp ở nhiều tầng | Người dùng mất thời gian tìm điều quan trọng | P0 UX |
| G03 | Doors header/filter chiếm nhiều diện tích trước opportunity đầu | Discovery yếu, ít card trên một viewport | P0 UX |
| G04 | Opportunity details diễn giải bằng paragraph và lặp `Manual source review` | Khó quét trạng thái/điều kiện | P0 UX |
| G05 | What-if 5 doors có **sơ đồ đầy đủ + 5 full detail cards** | Chiều dài tăng tuyến tính, lặp semantic | P0 UX |
| G06 | What-if diễn đạt `Needs action → Needs information` bằng rất nhiều chữ | Người dùng tưởng overall status không thay đổi nghĩa là không tiến triển | P0 UX |
| G07 | Active Plan có thanh tiến độ lấp ~1/3 khi mới `saved` | Có thể khiến người dùng hiểu nhầm hoàn thành 33% công việc | P0 truth/UX |
| G08 | Active Plan `Complete degree` chỉ có saved/started/complete | Chưa đủ cơ chế ghi nhận các bước thực hiện hữu ích | P1 product |
| G09 | Actual Update hiển thị raw ID `esa-student-internships`, `esa-availability`; Yes đang selected trong ảnh | UI kỹ thuật; cần xác minh liệu selected sẵn khi vào màn hay do thao tác trước | P0 kiểm chứng |
| G10 | Profile hiển thị `No doors` ở GPA/IELTS, lặp `Self-reported` từng fact | Có vẻ vô ích/mâu thuẫn demo dù chỉ do data rule coverage | P0 copy |
| G11 | Suggested verification kiểu `Reserve full required programme period` khó hiểu, `1 door affected` không cho thấy outcome | Đề xuất không tạo động lực | P0 product UX |
| G12 | Ước lượng 180 ngày có thể vượt official deadline; 5 doors simulation so với 4 doors plan | Cần giải thích phạm vi candidate/effect/feasibility một cách nhất quán | P0 truth |
| G13 | Nội dung `Your source of truth`, `sourced pack`, `Porta assumption` mang ngôn ngữ hệ thống | Khoảng cách nhận thức giữa user và engine | P1 writing |
| G14 | Thiếu branded visual storytelling tại các khoảnh khắc quan trọng | Thiếu điểm nhớ, chưa có chất riêng | P1 visual |

### 1.3 Nguyên tắc đo hiệu quả
- **5-second test**: người lạ nhìn 5 giây chỉ ra đúng tên, trạng thái và thao tác chính.
- **What-if 10-second test**: hiểu action giả định, 1–n requirements có thể cải thiện, overall eligibility còn cần gì.
- **No false confidence**: không nhầm *met encoded rule* với trúng tuyển/được provider xác minh.
- **Không đếm chữ máy móc**: ưu tiên giảm những đoạn lặp; text quan trọng về rủi ro và giới hạn kết luận vẫn luôn rõ.
- **Không đo layout bằng một emulator duy nhất**: test cả Android font lớn / viewport nhỏ và JVM Desktop resize.

---

## 2. Kiến trúc thông tin (IA) và thành phần chung

### 2.1 Sơ đồ luồng cần hỗ trợ

```text
Onboarding demo (sample: clearly fictional)
       ↓
Level 1
├─ Doors ────────────────→ Level 2: Opportunity details
│                             └─ Level 3: Full evidence / official-source details (nếu là route riêng)
├─ Next moves ──────────→ Level 2: What-if
│                             ├─ Add milestone to plan → Level 2: Active plan (theo navigation thực tế)
│                             └─ Evidence details → Level 3 (nếu push)
│                   └───→ Level 2: Active plan
│                             └─ Level 3: Update actual profile
└─ Profile ──────────────→ Level 2: Edit academic profile
                              └─ other data/source management routes (theo route graph thực tế)
```

**Lưu ý:** Nếu router hiện tại cho `What-if → Active plan` trực tiếp, nhãn level phải xác định theo vị trí trong navigation stack thực tế, không gán cứng route là Level 2 trong mọi đường đi. Báo cáo sử dụng độ sâu theo entry point phổ biến.

### 2.2 Disclosure chung
- **Glance (mặc định):** status có nghĩa, một action/tác động chính, deadline concern thật.
- **Understand (tap row):** các check đã đạt / chưa biết / cần làm, rule delta, remaining blockers.
- **Evidence (tap chi tiết):** nguồn, ngày đọc, version/cycle, exact thresholds, comparator, estimate basis, receipt.
- **Cảnh báo ảnh hưởng quyết định không được giấu ở Evidence**: deadline vượt estimate, unknown timing, actual result chưa xác nhận.

### 2.3 UI projection/data contracts cần thống nhất

| UI component | Dữ liệu nguồn cần nhận | UI mặc định | Quan trọng |
|---|---|---|---|
| `OpportunitySummaryCard` | opportunity ID/title/provider/category/overall status/important check/deadline/saved | compact card | Lấy next check từ model, không copy raw long rule |
| `StatusSummary` | hard-check aggregate + deadline/cycle state | short text, icon, color | Không tự suy ra eligibility từ count pass |
| `RequirementCheckRow` | requirement ID/result/short label/current vs expected/provenance | row ngắn + expand | `ManualReview` không giả là missing editable fact |
| `SourceDisclosure` | official URL/source section/checked at/cycle/timing | `Source` CTA, freshness warning khi cần | Reviewed != current valid |
| `ActionSummary` | action type, scope, affected IDs, impact, estimate/uncertainty | 1 target + preview CTA | Scope per-programme không gộp logic |
| `ScenarioOverview` | actual/scenario delta, counts, deadline feasibility, remaining checks | impact/change/remain | 0/1/5/10+ vẫn gọn |
| `PlanStateTimeline` | planned, in progress, work complete, actual update state | discrete steps | Không quy thành % completion giả |
| `ActualConfirmation` | actual, planned, selected achieved value, evidence model | explicit user confirmation | Default phải tránh tự nhận target đạt |
| `PrivacyControls` | scoped clear/reset/export behavior | nested settings | Tên action khớp thứ bị xóa |

**Reusable status names** cần dùng nhất quán giữa Doors → Details → What-if → Plan. `Needs information`, `Needs action`, `Source review`, `Archived` phải rõ thuộc đối tượng nào (rule, opportunity, source, cycle). Tránh dùng màu làm dấu hiệu duy nhất.

---

## 3. Chi tiết sửa LEVEL 1

### L1-A. Doors (P0 UX)

**Hiện trạng từ ảnh:** header `15 source-linked opportunities`, 2 câu mô tả, summary card `4 doors need action`, 4 filter chip hai hàng + `More filters`; card đầu gần cuối viewport. Các card có status, dài đoạn mô tả check và sometimes `SAVED` dưới ngôi sao đã fill.

**Thiết kế đề xuất:**
1. Header ngắn: `Your doors` + *một* subtitle `[15 opportunities · 4 need action]` khi counts chắc chắn. Nếu chưa có profile thật ghi `Explore with sample profile`, không nói `your saved profile`.
2. Một hàng filter chính `All · Action · To verify · Saved`, secondary trong `Filters` (archived/closed/other); sticky filter khi cuộn nếu khả thi, nhưng không che content.
3. Card compact: category glyph hoặc provider identity nhỏ; tên; provider + category; status; **một** short next check; deadline chip hoặc `Deadline unconfirmed`; bookmark toggle. Không chứa full official paragraph và checked-at dài.
4. Với card có nhiều manual checks, hiển thị `Review 3 additional conditions` hoặc `Eligibility varies by constituency` *nếu được model hỗ trợ*; tránh `Verify ·` + một paragraph raw bị truncate.
5. Đặt trạng thái Saved duy nhất ở bookmark (có accessible label `Saved`; có thể text khi show saved filter), không duplicate label.
6. Khi no matches: clear filters; khi source stale/unknown: warning cụ thể, không có `verified` vô căn cứ.
7. Desktop: rail + list/detail layout nếu hữu ích, không nhất thiết phải vẽ tất cả card full width.

**Sketch:**
```text
Your doors               [Filter]
15 opportunities · 4 need action
[All] [Action] [Verify] [Saved]
┌──────────────────────────────────┐
│ ◇ Kotlin Student Coding     [☆] │
│ Kotlin Foundation · Contest       │
│ [Needs information] [Dec 7]      │
│ Confirm enrollment conditions  → │
└──────────────────────────────────┘
```

**Acceptance L1-A:** 2–3 card có thể scan ở mobile tùy font; filters không lấn át content; mọi state vẫn truy cập được; bookmark không mất; official source/detail truy cập được; không có badge color-only.

### L1-B. Next moves (P0 product UX)

**Hiện trạng:** recommendation card đề xuất `Reserve the full required programme period` cho CERN; `1 door affected`, `Why this move?`, `Preview changes`. Growth note về 90/180 ngày, Active plan ở sau recommendation, alternatives cùng title lặp; constraints collapsed.

**Thiết kế đề xuất:**
1. **Context-aware first section**: nếu Active Plan đang `in progress`/`work complete pending actual confirmation`, ưu tiên `Continue your plan` hoặc `Confirm your result` phía trên suggestions. Nếu plan `planned`, có thể ưu tiên một resume card nhỏ hoặc xếp cùng suggestion theo rule rõ. Không để completed-but-unconfirmed bị chôn.
2. Recommended CTA chỉ khi có recommendation đủ basis; nếu chỉ có VERIFY thì label `Suggested verification`, không `Best move`.
3. Đặt tên action theo *mục đích user*: `Confirm your availability for CERN` nếu scope chỉ là xác nhận. Giữ display distinction với action domain ID; không tự thay đổi behavior.
4. Điểm nhấn visual **Action → Rule affected → Opportunity**; 1 affected => một nhánh nhỏ, multiple => mini Ripple. Phải kèm trạng thái remaining checks.
5. `1 door affected` cần diễn đạt đúng hơn `1 opportunity · 1 check could change` nếu đúng số; không dùng claim tổng quát nếu action chỉ giải một check.
6. `Low/Medium/14 days` chỉ xuất hiện cùng ý nghĩa rõ ràng; unknown estimate không được fake; provenance trong `Why this move?`.
7. Alternatives grouped theo action family, mỗi row chỉ title chương trình/short difference, mở chi tiết theo scoped ID. Tránh 3 full repeated paragraphs.
8. Growth note rút còn `Longer-term moves need more than 90 days` + `Compare 180 days`; nếu có deadline caution phải hiển thị.

**Acceptance L1-B:** sau 5 giây user biết *hành động cụ thể làm gì* và *tác động theo encoded check đến đâu*; không còn 3 bản copy giống nhau; completed-plan được ưu tiên hợp lý; `Any cost` không biến estimate unknown thành low; plan navigation hoạt động.

### L1-C. Profile (P0 writing, P1 hierarchy)

**Hiện trạng:** card `Computer Science student`, `9 facts`, `13 doors`, sections Essentials/Academic record/Context; từng fact lặp `Self-reported`, `Details`, `No doors`; Privacy & data nằm thành block dài cuối màn.

**Thiết kế đề xuất:**
1. Giữ card hero cá nhân hóa; có thể thêm abstract **Personal Atlas** nhỏ, không bắt buộc cho phiên bản đầu.
2. Dòng `9 facts · 13 doors` phải đổi thành count có định nghĩa rõ như `9 facts provided · 13 opportunities checked` *chỉ khi thật sự đúng*; bằng không đổi label theo semantics hoặc ẩn door count.
3. Một tóm tắt hữu ích lên trên: `1 missing fact may affect 2 checks` (không suy diễn `2 doors` nếu thực tế là requirements).
4. Sections dạng rows ngắn `Fact name · Value · [impact count nếu >0] · chevron`; không lặp `Self-reported · Changes with time` trên mỗi dòng. Provenance có một statement chung và detail-specific distinctions.
5. `No doors` thay bằng **không hiển thị counter khi 0**. Trong details có thể giải thích `No rules in this pack currently use IELTS`; không nói IELTS không bao giờ quan trọng.
6. Không đưa cảnh báo `Not checked yet` quá lớn nếu chỉ một optional missing value; CTA `Add experience` nếu thực sự có rule mapping.
7. Privacy controls nên tách màn `Data & privacy`, vẫn truy cập ≤2 taps từ Profile. Delete/reset phải confirm destructive effect.
8. Không hiển thị nhiều title serif ngang mức chính; section names ngắn, sans cho rows.

**Acceptance L1-C:** rõ cái gì có/thiếu và vì sao nhập thêm hữu ích; IELTS không bị coi vô dụng; không nhầm 13 với 15; Privacy controls không đẩy core profile xuống dưới; save/edit entry rõ.

---

## 4. Chi tiết sửa LEVEL 2

### L2-A. Opportunity details (P0 UX)

**Hiện trạng:** hero `Opportunity details` lớn rồi tới title cơ hội lớn; `Current conclusion` card; `What to verify next`; raw manual notes; `Evidence behind this conclusion`; `Source and last review`; source review dates lặp nhiều lần.

**Thiết kế đề xuất:**
1. App bar nhỏ `‹  Opportunity` hoặc `‹ Details`; tên chương trình là hero duy nhất. Không có hai large serif headlines liên tiếp.
2. Hero: title, provider, type, known/unknown deadline; bookmark. Một status strip riêng phân biệt `rule assessment` và `timing`.
3. **Your checks**: các `RequirementCheckRow` nhóm `Met`, `Needs action`, `Needs information`, manual review. Hiển thị 1-2 hàng title/short meaning; expand row để xem original source wording, current value, comparator, URL.
4. Nếu chưa đủ information để gợi ý action: message ngắn `No reliable profile change suggested yet`; CTA `Review official conditions` nếu có nguồn, không cần section văn dài.
5. Nếu có action (ví dụ Schwarzman: complete degree): đặt dưới impacted unmet check với CTA `Preview impact` / `See next step`; tránh button `Complete the required first degree` khiến người dùng nghĩ nút xác nhận hoàn thành bằng cấp ngay.
6. `Manual source review` là **state của requirement**, không hiển thị `Your current value: Not provided` nếu không có user fact nhập được.
7. Source and evidence gộp vào một `Evidence & sources` disclosure, không che official link nhưng không show full paragraph mặc định.
8. Selection soft factors collapsible, không dùng pass/fail icon cho những yếu tố này.
9. Đặt short trust note một lần; không lặp trong từng row.

**Acceptance L2-A:** thấy `what's met? what needs attention? what next?` trong một viewport; full receipts/official URL vẫn có; `Needs source review` không bị đánh đồng `Not eligible`; age không hiển thị `20.0 years`.

### L2-B. What-if (P0 UX — quan trọng nhất)

**Hiện trạng:** 1 Door: impact arch + lại một full detail card. 5 Doors: 5 impact tiles nối path + 5 full detailed cards; mỗi card lặp `Actual`, `Then`, `Simulated`, `Would address ...`, `What changes`, `What remains`, `Review evidence`; screen dài nhiều viewport. Một case có warning `180-day estimate extends beyond deadline`.

**Cấu trúc mới, không tạo double rendering:**
1. Top banner nhỏ `Simulation · Your profile won't change` (một lần, không lặp bên dưới mỗi node).
2. Hero outcome: `5 opportunities affected` (không dùng `5 doors move forward` nếu một cơ hội đã quá deadline hoặc progress chỉ rule-level); subtitile `X checks could change · Y still need information · Z timing concern(s)` có count thật.
3. **Before/after fact**: ví dụ `First degree completed: No → Yes (hypothetical)`, tách với overall opportunity status.
4. **Action ripple** là visualization rút gọn: 1 action node + tối đa 3–5 mini nodes compact; với >5 nhóm `+N more`, tránh arch card cao 180dp mỗi node. Với 0 thì no-impact state. Mỗi node có name/short state marker và nếu cần một deadline concern glyph; không lặp raw paragraph.
5. **Results**: một compact list indexed từ đúng `affectedOpportunityIds`, không render lại 5 full cards. Tap row => expandable selected detail hoặc một detail panel (Desktop). Chỉ mở **một opportunity detail** tại một thời điểm trên mobile.
6. Selected detail chỉ hiển thị (a) changed check(s) (b) what remains (c) deadline concern/estimate basis; exact receipt trong `Review evidence`.
7. Overall status may remain `Needs information` hoặc chuyển `Needs action → Needs information`. UI nên ưu tiên `1 assessed requirement improved` trước, có ghi `Overall status still pending` đúng nơi.
8. Deadline-specific row (World Bank): `Estimated timeline may exceed the published deadline` trong summary và row; phân biệt `estimate assumption` với official confirmed deadline. Những cơ hội bị quá hạn không được trình bày như khả thi ngang với các case khác.
9. CTA cuối: `Add milestone to plan` nếu real profile và milestone legitimate; riêng case deadline conflict cần giải thích trước khi cho commit hoặc disable theo policy. `Create profile to save` khi sample. Secondary `Discard scenario`.
10. Về kỹ thuật, danh sách và diagram **phải cùng một UI projection**, tránh hai bộ tính/trạng thái khác nhau.

**Responsive:** Android nhỏ => summary + compact list + expansion; desktop => diagram trái, selected rule diff bên phải, action footer trong content container. Không sticky header che nội dung khi scroll.

**Bộ bắt buộc test:** 0, 1, 5, 12+ affected; 1/2+ deadline concern; overall unchanged; direct `NEEDS_ACTION→NEEDS_INFORMATION`; 5 affected nhưng một estimate misses deadline; unknown estimate; sample synthetic 4 IELTS rules; actual profile unchanged.

**Acceptance L2-B:** chiều dài **default** không tăng một card full-height cho mỗi opportunity; người dùng biết 5 ảnh hưởng ở glance, có thể mở chính xác từng source/check; không còn 2 lần cùng một đoạn cảnh báo; không khẳng định eligibility mở; screen reader tương đương diagram.

### L2-C. Edit academic profile (P1 UX)

**Hiện trạng:** `Edit academic profile` + headline `Describe where you are now` + paragraph giải thích, Essentials, optional cards Academic Record / Experience and Evidence / Context, local-first note và save CTA; khi expand Experience có nhiều text input liên tiếp.

**Thiết kế đề xuất:**
1. App bar `Edit profile`, một subtitle ngắn `Update what you know today`; bỏ `YOUR SOURCE OF TRUTH` (user self-report ≠ verified truth).
2. Essentials hiện ngay, các optional groups collapsed và có count `4 of 4 provided` v.v.; không lặp paragraph dài vì unknown semantics có thể hiển thị ở một info disclosure.
3. Khi open Experience & Evidence, chia subgroups `Experience`, `Project`, `Leadership`; label gắn đúng field; cho phép nhập ngắn, sample hint cụ thể, scroll tới first relevant input.
4. Input validation theo type: GPA scale 4/10, IELTS 0–9 increments if score field, age/date semantics, nationality. Không tự convert GPA.
5. `Save profile` rõ trạng thái; nếu disabled cho biết lỗi cần xử lý. Không báo đỏ chỉ vì optional field trống.
6. Footer privacy note một dòng, chi tiết qua Data & privacy. Sau save quay về Profile với toast/snackbar xác nhận, không tuyên bố external verification.

**Acceptance L2-C:** bắt đầu edit nhanh, người dùng không phải điền leadership nếu không muốn; optional unknown không thành FAIL; focus/keyboard không che field hoặc CTA; no invalid auto-conversion.

### L2-D. Active Plan (P0 progress truth, P1 interaction)

**Hiện trạng:** Planned card, date/target, 180 days high/medium chips, progress bar đã lấp ~1/3 chỉ vì saved, checklist saved/started/completed; evidence + affected doors là 1 block raw names; risk note dài; actions Start, Preview again, Remove.

**Thiết kế đề xuất:**
1. Hero `Current milestone`, trạng thái rõ `Planned / In progress / Work complete / Actual confirmed` và target human-readable.
2. **Không dùng phần trăm từ 3 checkbox trạng thái**. Nếu cần visual, dùng discrete stepper (Planned → In progress → Completed → Confirm actual); chỉ đánh dấu đúng state đã đạt. Tiến độ học hành nhiều tháng không được suy từ trạng thái save.
3. Thông tin ước lượng thành các label/value có đơn vị: `Estimate 180 days (Porta assumption)`, `Effort High`, `Cost Medium`; không show high/medium không nhãn.
4. `Next action` phải phụ thuộc trạng thái: planned = `Start milestone`; in-progress = `Mark work complete` (có confirm); work complete = `Confirm actual result`; actual confirmed = `See updated opportunities`.
5. Affected doors dùng compact list/tiles; số lượng có nghĩa chính xác: `4 in-plan opportunities` vs `5 affected in earlier scenario` nếu loại một vì deadline. **Phải giải thích phạm vi đếm khác nhau** nếu same plan/scenario; nếu ảnh đến từ các plan khác thì không coi đó là bug.
6. Evidence requirements chỉ hiện khi action/milestone cần; tránh full paragraph luôn xuất hiện. Risk note ngắn, expandable; deadline caution hiển thị trên hero khi thực sự quan trọng.
7. Danger `Remove plan` đưa cuối, confirmation dialog, mô tả cái gì bị xóa (plan hay actual fact?), không accidental tap.
8. Nếu muốn milestone chi tiết, cho phép **user-defined mini checkpoints** tối giản (không tự bịa 180-day schedule). Vẫn một active milestone theo MVP; history là extension riêng nếu scope cho phép.

**Acceptance L2-D:** không false 33%; CTA đúng state; completed work không auto-mutate actual; timeline có accessible labels; scope/count rõ; evidence local policy đúng implementation.

### L2-E. Pack/source inspector, saved/filter states (nếu là Level 2 theo router)
- Pack Inspector là công cụ chuyên sâu: giữ tables, provenance, diagnostics, search và version/cycle; không cần illustration decorative.
- Source freshness: `reviewed` vs `current cycle verified` vs `deadline unconfirmed`, không gom thành green badge chung.
- Saved empty, no recommendations, no filter results: dùng empty illustration A03/A04 và một CTA hữu ích, không generic marketing.
- Confirmation dialogs cho import/replace/reset nêu chính xác dữ liệu sẽ thay đổi; invalid pack không phá active pack.

---

## 5. Chi tiết sửa LEVEL 3

### L3-A. Update Actual Profile (P0 truth + P1 layout)

**Hiện trạng:** banner `PLAN COMPLETE · PROFILE STILL UNCHANGED`, rule raw ID chương trình ESA, Current actual vs planned target, `What did you actually achieve?` Yes/No, evidence card có label required + note, green explanation paragraph, disabled confirm button, cancel CTA.

**Thiết kế đề xuất:**
1. App bar `Confirm actual result` hoặc `Update your profile` (tuỳ semantics), một câu giới thiệu: `Your plan is complete. Confirm what is true now.`
2. Trình tự form: **What changed? → Actual value → Evidence (if required) → Confirm**. Ẩn detail quá kỹ thuật dưới `Why is evidence needed?`.
3. Scope human-readable: `ESA Student Internships · Programme attendance`, không raw `esa-student-internships`/`esa-availability`; IDs nằm ở evidence/debug nếu cần.
4. Hiển thị side-by-side `Current: Not confirmed` / `Target: Yes` là ổn. Nhưng **không tự chọn Yes chỉ vì planned target = Yes**. Nếu screenshot Yes là do thao tác trước thì ghi lại behavior xác minh; acceptance là không có silent default.
5. Nếu result có 3 trạng thái `Yes / No / Not sure`, hỗ trợ Not sure khi domain model cho phép, hoặc rõ user có thể quay lại không cập nhật. Không tự ép selection No khi chưa rõ.
6. Với requirement scope-specific, nếu evidence label là bắt buộc, đánh dấu `Required` cạnh field trước lúc người dùng nhập; lỗi đỏ chỉ khi touched/submit và invalid. Reference/note optional theo model; không gọi metadata note là `verified proof`.
7. Một disclaimer ngắn trước CTA: `You'll update your self-reported profile; Porta will recheck affected rules.` Tránh panel lớn màu xanh giải thích lại cùng ý.
8. Sau confirm hiển thị **result screen/feedback**: `Actual fact updated` + count assessed checks changed / unchanged / remaining, nếu engine trả dữ liệu; có CTA `View updated doors` và `Back to plan`. Không nói `Eligibility confirmed` nếu manual blockers còn.
9. Cancel = `Keep actual profile unchanged`; explicit back navigation và unsaved form handling.
10. Nếu plan đã hoàn thành nhưng actual differs from target, lưu **achieved value**, không target; test reevaluation chính xác.

**Acceptance L3-A:** không silent Yes; không có raw IDs; evidence model đúng; no mutation trước confirm; confirm tạo reactive reevaluation, không tự chuyển unknown thành provider-verified; form errors chỉ hiện đúng lúc.

### L3-B. Evidence & source details / expanded drill-down
- Hiển thị original exact requirement, official source URL, section, checked timestamp, cycle, encoding method, and uncertainty; thông tin chính xác từ model.
- Có đường back ra đúng Details/What-if scroll position, không push infinite nested screens.
- Chuẩn hóa copy `Reviewed 21 Oct 2026` không tự đồng nghĩa `Officially verified now`.
- JSON/raw IDs chỉ ở chế độ diagnostics, không để trong user-facing labels.

---

## 6. Onboarding: chỉnh đủ để nhất quán với sản phẩm thật

- Step 1: chọn focus với icon học bổng/thực tập/cuộc thi, ít chữ, một primary CTA.
- Step 2: slider IELTS 6.5→7.0 + threshold marker, **4 fictional nodes** phản ứng khi sample rule đạt ngưỡng.
- Step 3: summary `4 sample IELTS checks changed` thay vì hàm ý `4 applications unlocked`; spotlight before/after.
- Step 4: `Create profile` / `Explore sample` phân cấp; stored-local statement một lần.
- Demo **fictional** phải được nhắc đúng chỗ; real pack hiện không có cùng IELTS coverage theo context cũ. Không quảng cáo tác động synthetic như khả năng sẵn có trên real data.
- Chỉ thêm hero art ở onboarding và empty state; đừng thêm ảnh minh họa rộng cho mọi opportunity list item.

---

## 7. Art direction & design system

### 7.1 Visual language đề xuất: Editorial Thresholds
- **Arch/Door:** nhận diện opportunity, nhỏ trên card hoặc hero visual.
- **Threshold:** ngưỡng như IELTS hoặc degree requirement, không ám chỉ được nhận học bổng.
- **Path:** nối hành động với một requirement.
- **Ripple:** một action ảnh hưởng nhiều opportunity khi scenario engine thực sự tính ra nhiều.

### 7.2 Asset set giới hạn

| Asset | Dùng ở đâu | P | Trạng thái truth |
|---|---|---|---|
| Threshold Doors | onboarding IELTS demo | P1 | Fictional, 4 sample rules |
| Personal Atlas | empty Profile/profile hero | P2 | Abstract identity, không số liệu giả |
| Action Ripple | Next moves + What-if | P1 | Nodes từ affected IDs thật |
| Plan Path | Active Plan | P1 | Status thực tế, không fake progress % |
| Empty Saved/No Action | Saved/no relevant move | P2 | Static fallback |

**Production:** Compose Canvas/vector hoặc licensed local assets; static dark/light and reduced-motion variants; labels độc lập hình. Không cần generic người cầm laptop, 3D blob, cờ quốc gia hoặc fake provider logo.

### 7.3 Typography / spacing / shape
- Serif dùng cho heading chính/tên selected opportunity; sans cho status, body, form, card list và buttons.
- Text line-height và content width ưu tiên readability; không dùng headline serif rất lớn ở cả app bar lẫn hero.
- Một primary CTA trong viewport; secondary CTA dạng outline/text rõ ràng.
- Neutral surfaces chiếm đa số; lavender dùng brand action, teal dùng **rule-level progress** đã được xác nhận theo evaluator, amber cho requires action/verification, red cho error/timing risk.
- Đừng tô tất cả changed opportunities amber nếu semantics của chúng khác nhau: visual node color phải phản ánh *status type*, không chỉ chung “affected”.
- Android touch targets đủ lớn, chip không phải tap quá sát; spacing không tạo 1 màn chỉ thấy 1 card.
- Desktop content max width theo tác vụ: form không kéo full window, comparison có thể 2 columns, list/detail master-detail.

### 7.4 Motion (P2)
- Tap/selection 120–160ms; expand 150–220ms; scenario connector reveal 180–300ms; bookmark 120–180ms.
- Không animation loop, confetti, animated open door khi overall còn unknown hoặc deadline không khả thi.
- Reduced-motion respect; TalkBack phải đọc outcome như text, không dựa vào hình vẽ.

---

## 8. Product truth/engineering checklist (phải làm song song)

**Các ghi nhận từ repo context 24/10 là static trace, cần đối chiếu với current working tree:**

| Contract | Hành động kiểm tra/sửa | Test bắt buộc |
|---|---|---|
| Unknown != fail | Giữ eval state và UI copy | Profile thiếu fact → needs info, không fail |
| Rule met != opportunity open | Tách assessment vs timing | 1 check pass, 2 manual remain → no OPEN |
| Scenario != actual | Không ghi local fact khi preview/add plan | Reload app, actual unchanged |
| Plan complete != actual confirmed | Chỉ cập nhật sau Actual Update | Mark complete alone doesn't mutate profile |
| Actual achieved != target | User chọn value thật | Target Yes, actual No → No saved |
| Scoped action isolation | ESA availability không pass OECD/CERN | Cross-programme isolation |
| Source reviewed != current verified | Date/pack freshness riêng timing | TBA → not automatically active |
| Deadline feasibility | Compare proper reference dates and uncertainty | 180-day assumption exceeds published deadline → caution |
| Estimates are assumptions | Type/range/unknown provenance | Empty unknown never painted `Free/Low` without basis |
| Plan progress | No artificial 1/3 save = work complete | Planned status baseline has no work progress |
| Count definition | 5 affected vs 4 in-plan if filtered by timing | Snapshot test explains excluded count |
| Profile utility | `No doors` suppressed when 0, not falsely claimed global effect | GPA/IELTS no typed rules current pack |
| Evidence levels | self-attested vs evidence reference vs provider verified | evidence label alone doesn't claim verified |
| Local-first promise | Verify inherited startup/network path, actual storage | Offline cold launch + no unauthorized network side effects |
| Desktop persistence | Report previously found JVM DB in temp folder | Restart app, profile/plan still exist |
| Imported packs | Atomic validation; IDs/scopes and version | invalid pack preserves old active pack |

**Data track riêng:** 15 real sourced opportunities nhưng typed coverage hạn chế theo report cũ. Cần curated 3–5 deep opportunities có source/current cycle/typed conditions thật, ít nhất một genuine cross-door action, vẫn giữ complex rule ở manual. Không invent threshold GPA/IELTS. Data refresh qua versioned pack có thể online-enhanced sau MVP; core offline-capable vẫn giữ.

---

## 9. Kế hoạch triển khai theo wave và dependency

### Quy tắc chung
- Mỗi wave bắt đầu bằng **VERIFY** source trace và screenshot baseline hiện tại; sau đó implementation nhỏ, tests, ảnh before/after.
- Không cho Codex làm một PR thay toàn bộ app. Ưu tiên mỗi commit một vertical slice hoặc một shared component.
- Tách **UI refactor** với **domain semantics changes** nếu có; semantic change cần tests rõ.
- Các wave sau chỉ bắt đầu sau khi gate wave trước đạt, trừ visual asset R&D có thể song song.

| Wave | Scope chính | Phụ thuộc | Deliverable | Gate |
|---|---|---|---|---|
| **W0 — Baseline & truth** | Route map, screenshot baseline, status/estimate/count truth audit, duplicated copy inventory, test existing flows | — | `PORTA_CURRENT_STATE_VERIFY.md`; screenshots; passing known build suite | Không còn critical semantic ambiguity chưa được ghi |
| **W1 — Level 1** | Doors compact, Next moves active-plan ordering + concise recommendations, Profile copy/count/privacy IA | W0 | Shared `OpportunitySummaryCard`, `ActionSummary`, revised 3 tabs | Scan 5s, source discoverable, counts correct |
| **W2 — Level 2 interpretation** | Opportunity Details checklist, What-if scalable summary/list, Edit Profile progressive | W1 + verified data projection | `RequirementCheckRow`, `ScenarioOverview`, expanded receipts | 0/1/5/12 scenarios và official source test pass |
| **W3 — Progress & confirmation** | Active Plan timeline, Update Actual Profile, meaningful feedback, evidence validation | W0 + W2 | `PlanStateTimeline`, `ActualConfirmation` | No silent actual changes, count semantics consistent |
| **W4 — Visual identity & motion** | Selective Arch/Threshold/Ripple/PlanPath, onboarding and empty states, semantic animation | W1–W3 stable | Local vectors + reusable components | No decorative/data conflicts, reduced motion |
| **W5 — QA & release** | Android/desktop responsive, accessibility, localization copy, screenshots, build/test, source/data correctness review | W0–W4 | Tested release candidate + notes | Gates below complete |

**Thứ tự sửa ưu tiên nếu deadline ngắn:** W0 (truth critical) → W2 What-if scalable + L2 Details → W1 Doors/Next moves/Profile → W3 actual/plan → W4 polish → W5 QA. Wave numbering là nhóm quản trị; triển khai W2 trước W1 cho 1 component nếu yêu cầu, nhưng giữ dependencies của data projection và tests.

### Task backlog có thể giao Codex

| ID | Task | P | Size | Acceptance rút gọn |
|---|---|---|---|---|
| T01 | Audit current navigation, sources, counts, estimate truth | P0 | M | Verify matrix + tests; no guessed assumptions |
| T02 | Centralize display status labels/short reasons/provenance | P0 | M | Same semantics across 4 screens |
| T03 | Doors compact header/filter/cards | P0 | M | Scan opportunities quickly; secondary filters accessible |
| T04 | Next moves plan priority + action naming/grouping | P0 | L | Scoped actions preserved; resume CTA correct |
| T05 | Profile counts and no-doors wording | P0 | S | No misleading zero or 13/15 discrepancy |
| T06 | Details grouped requirement checklist + source disclosure | P0 | L | Full evidence still accessible |
| T07 | What-if single source-of-truth projection | P0 | L | One data list for diagram + detail |
| T08 | What-if compact multi-door result, selected expansion | P0 | L | 0/1/5/12+ viewport behavior, warning visible |
| T09 | Plan progress state truth & timeline | P0 | M | Save does not mean 33% work complete |
| T10 | Actual Update explicit result + human labels + validation | P0 | M | No silent Yes; achieved value tested |
| T11 | Edit Profile short-form progressive validation | P1 | M | Optional not required; keyboard/focus |
| T12 | Shared icon/status/illustration primitives | P1 | M | semantic fallback, dark/light |
| T13 | Action Ripple & visual impact | P1 | L | Only actual affected IDs; screen reader label |
| T14 | Active plan affected opportunities + risk simplification | P1 | M | count scope/timing understood |
| T15 | Onboarding sample clarity and threshold visual | P1 | M | Fictional status, visual delta, real promise consistent |
| T16 | Data & privacy nested route | P1 | S–M | Destructive actions precise and confirmed |
| T17 | Adaptive Android/Desktop QA, font scale/contrast/motion | P0 release | L | No clip/overlap/focus trap |
| T18 | Source pack and recommendation utility improvements | Parallel P0 product | L | Real-source supported cross-door progress |
| T19 | Local startup/persistence reliability | Parallel P0 engineering | M–L | Cold offline, durable desktop profile |

**Size:** S nhỏ, M trung bình, L lớn (không phải cam kết thời gian).

---

## 10. UX Writing dictionary / microcopy đề xuất

> Các câu là định hướng; Codex phải chọn theo dữ liệu/status thật, không thay thế domain fact.

| Context | Trước | Sau — đề xuất |
|---|---|---|
| Home data | `15 sourced doors` | `15 opportunities with official source links` hoặc `15 opportunities` |
| Uncertainty | `Needs information` | giữ, giải thích ngắn `Confirm enrollment details` nếu đúng |
| Source | `within Porta's 90-day review window` mỗi card | `Source reviewed 21 Oct` trong source section |
| Detail app bar | `Opportunity details` khổng lồ | `Details` trên app bar, tên opportunity là hero |
| Short next move | `Reserve the full required programme period` | `Confirm availability for [programme]` nếu đó là fact được kiểm |
| Impact | `1 door affected` | `1 opportunity · 1 rule could change` nếu exact |
| Estimate | `Est. 180 days · Porta assumption` | `Estimated: 180 days (Porta assumption)` trong detail |
| Profile zero | `No doors` | Ẩn badge 0; detail `No current rules use this fact` |
| Profile data | `9 facts · 13 doors` | label số nào tính được đúng: `9 details provided · 13 checked opportunities` nếu grounded |
| Source of truth | `YOUR SOURCE OF TRUTH` | `Your current details` |
| Hypothetical | `5 doors move forward` | `5 opportunities affected` / `5 checks would improve` theo metric |
| Unchanged overall | `Needs information → Needs information` | `One rule improves · Overall status still needs information` |
| Deadline conflict | `This estimate misses the deadline` | `Estimated timeline may exceed the published deadline` nếu estimate không chắc |
| Plan marker | `Progress checklist` | `Plan status` / `Milestone status` |
| Actual update | `Programme esa-... requirement esa-...` | `ESA Student Internships · Attendance requirement` |
| Actual CTA | `Confirm actual profile update` | `Save my confirmed result` (chỉ khi user self-attested) |
| Confirmation | `Doors will immediately re-evaluate` | `Porta will update the relevant checks` |

**Bắt buộc sửa lỗi grammar:** `1 door` thay vì `1 doors`; số nguyên không `.0` trừ khi scale/score đòi hỏi (IELTS 7.0 hợp lệ).

---

## 11. Kiểm thử và release gates

### 11.1 UI states bắt buộc

| Journey | Cases |
|---|---|
| Doors | sample vs real; filters All/Action/Info/Saved; archived/closed; long provider/name; missing deadline; card saved; no results |
| Details | fully typed pass subset; needs action; manual review; deadline TBA; archived; exact URL source; no safe action |
| Next moves | verify-only, real improvement, no viable move, unknown estimate, scope-specific duplicate title, active plan in progress, completed pending confirmation |
| What-if | 0/1/5/12+ affected; 1/2+ deadlines; same overall status; different overall status; source review unresolved; sample IELTS synthetic; actual unchanged |
| Plan | planned/in-progress/completed/paused if supported; cancellation; reopen; 4 vs 5 affected scope; no fake percent |
| Actual Update | user chooses No vs target Yes; field untouched; evidence missing/touched; actual update; canceled update; wrong evidence ref; profile recompute |
| Profile | missing required/optional values, zero-used facts, GPA 4/10, IELTS decimals, nationality, export/reset, device restart |
| Import/pack | valid/invalid import; version change; same ID conflict; atomic rollback; source freshness |

### 11.2 Platform/accessibility
- Android compact 360dp and narrow 320dp if supported; ≥1.3× and ≥1.5× font scale; screen rotation/insets/keyboard.
- JVM Desktop minimum supported width, medium and wide; nav rail behavior; resize 900→600; keyboard tab focus; scroll region correct.
- Dark + light contrast; not color-only; TalkBack reading status and impacts; touch target ≥48dp where applicable.
- Reduced motion; offline cold launch; app relaunch persistence; no screenshot state relying on random data.

### 11.3 Trước khi merge mỗi wave
1. Identify exact screens/viewmodels/models files changed.
2. Confirm domain truth hasn't changed unless explicitly documented and tested.
3. Run applicable repo build & tests; paste command and outcome; **không báo PASS nếu chưa chạy**.
4. Capture Android screenshot before/after; desktop screenshot cho changed responsive screens.
5. Review with three questions: `Tôi đang xem gì?`, `Tôi nên làm gì?`, `Tôi chưa biết điều gì?`.
6. Maintain `PORTA_UI_CHANGELOG.md` + decision log.

---

## 12. Những việc KHÔNG nên làm lúc này

- Full visual rebrand, đổi font/brand color không có lý do.
- Hàng chục stock illustration hoặc ép logo tất cả 15 chương trình.
- Gamified `100% eligible`, unlock/gold/confetti khi chỉ một encoded check thay đổi.
- AI chatbot, crawler, cloud sync bắt buộc cho core, hoặc predictive acceptance rate để “đẹp demo”.
- Chuyển tất cả màn thành form wizard dài hơn.
- Xóa nguồn/receipt/caveat để UI trông sạch nhưng sai.
- Gộp scoped programme confirmations thành một universal fact.
- Thêm estimated days không có căn cứ hoặc sửa deadline để có nhiều “open doors”.
- Cố làm tất cả tasks trong một lần gọi Codex mà không có checkpoints.

---

## 13. Thứ tự bàn giao/triển khai thực tế

**Nếu cần chọn ba lát cắt để chứng minh hiệu quả ngay:**

**Slice A — Doors → Details:** compact card, no repeated texts, check rows, official source accessible. Người dùng chọn cơ hội và hiểu 1 screen mà không đọc report.

**Slice B — Next moves → What-if (1/5/12 doors):** một recommendation cụ thể, scenario visual rõ, list compact, expandable source, deadline caveat nổi bật. Đây là *signature moment* của Porta.

**Slice C — Active Plan → Actual Update:** timeline có nghĩa, completed-but-unconfirmed, confirm achieved value không auto-target, cập nhật lại relevant opportunities. Đây là *retention/value loop*.

Xong ba slice mới thực hiện animation, art assets, enhanced onboarding. Đó là cách đảm bảo visual không che yếu kém của thuật toán hoặc dữ liệu.

### Quyết định cuối

**Goal:** *Less reading, more seeing — without less truth.*
**North star:** *A student can see an opportunity, understand the exact gap, preview a grounded action, and track a real outcome without learning PORTA's internal implementation vocabulary.*
