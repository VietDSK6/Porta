# PORTA — Visual UX & Art Direction Blueprint

> Historical design artifact. Retained for provenance; the shipped UI and current PRD remain authoritative.

**Bản đặc tả cải thiện UI/UX và tính hấp dẫn thị giác, dùng cho Android + JVM Desktop**
**Ngày biên soạn:** 26/10/2026
**Cơ sở:** 9 screenshot mới nhất (Doors, Opportunity details, Next moves, Limits, What-if), ảnh onboarding và Profile các vòng trước, `PORTA_PRODUCT_CONTEXT.md` snapshot 24/10 và lịch sử review.
**Mức chứng cứ:** `[ẢNH]` = quan sát từ screenshot; `[REPO-REPORT]` = Codex đã static-trace và viết trong context, chưa kiểm chứng runtime ở vòng này; `[ĐỀ XUẤT]` = quyết định thiết kế cần validate. Không lấy bản thiết kế làm bằng chứng về tính đúng của dữ liệu eligibility.

---

## 0. Executive direction

**Chẩn đoán:** Porta đã có nền tảng thị giác nhất quán (dark surface + lavender + Newsreader/Manrope), nhưng đang lặp lại một nhịp "heading → đoạn văn → card tối → metadata → nút" quá nhiều. Các wave sửa trust dẫn tới text overload; UI mô tả cách hệ thống suy luận hơn là cho người dùng *nhìn thấy* điều đã thay đổi.

**Mục tiêu:** Chuyển từ *document-style UI* sang *editorial, visual, action-first UI*. Không thêm ảnh ngẫu nhiên. Giảm chữ ở tầng mặc định; giữ bằng chứng trong các lớp expandable. Thiết lập những biểu tượng riêng của Porta: **arch / threshold / pathway / ripple**.

**Một câu art direction:**
> A quiet editorial atlas of possibilities: architectural arches, precise paths and meaningful transitions — confident, restrained, human, source-aware.

**Ba hiệu ứng nhận thức người dùng phải đạt:**
1. Trong vài giây biết đây là cơ hội/hành động nào và trạng thái chung ra sao.
2. Biết điều gì có thể làm tiếp mà không đọc full rule receipts.
3. Khi thực hiện What-if, *nhìn thấy* rule nào thay đổi và rule nào vẫn còn chưa rõ, không nhầm scenario với sự thật.

**Không biến Porta thành:** stock SaaS illustrations, generic finance dashboard, gamified RPG "unlock 100%", social feed, "AI predicts scholarship", hoặc một app với ảnh lớn trong mọi card.

---

## 1. Hệ thống thông tin: giảm chữ đúng cách

### 1.1 Ba tầng disclosure

| Tầng | Khi nào hiển thị | Được phép chứa | Không nên chứa |
|---|---|---|---|
| **Glance** (mặc định) | Ngay khi mở list/detail/preview | Tên, loại, status chính, hành động/điều kiện nổi bật, deadline nếu xác nhận | Paragraph dài, rule tree, source note đủ câu, thuật toán, assumptions lặp lại |
| **Understand** (tap row/expand) | Người dùng muốn hiểu | Checklist grouped, before/after, số rule thay đổi, remaining blockers, vì sao rank | Serialization details, full raw provenance |
| **Evidence** (disclosure/tap source) | Người dùng cần tin cậy/kiểm chứng | Exact rule, nguồn, ngày đọc, assumptions, scope, provenance, full receipts, raw values | Lời khuyên chưa được nguồn chứng minh |

**Ràng buộc quan trọng:** Không ẩn caveat có thể gây quyết định sai. Nếu deadline chưa xác minh / scenario chỉ là hypothetical / có manual blocker ngăn xác định eligibility, phải có tín hiệu ngắn hiển thị ngay trong Glance. Đưa chi tiết giải thích vào Evidence.

### 1.2 Quy tắc biên tập

- Một màn hình có **một câu hỏi chính**: Doors = cơ hội nào đáng xem; Detail = tôi đang ở đâu; Next moves = hành động nào đáng cân nhắc; What-if = cái gì thay đổi.
- Một khu vực chỉ có **một primary CTA**; các CTA khác là secondary/tertiary.
- Không lặp cùng thông tin dưới title, trong status panel, trong paragraph và footer.
- Thông tin mức hệ thống như "Porta assumption", timestamp, raw source link chỉ cần ở nơi liên quan, với cue rõ ràng khi cần.
- Không tự động render câu `"14 other doors unchanged"` thành một paragraph lớn; có thể dùng `Other opportunities unchanged · 14` ở disclosure.
- Nén chữ **bằng cách đổi biểu diễn**, không giảm kích thước toàn UI.
- Chỉ thể hiện số lượng nếu nguồn dữ liệu tính được chính xác; không hiển thị count ảo.

### 1.3 Khuyến nghị về copy

| Hiện tại quan sát trong screenshot | Tầng Glance đề xuất | Tầng sâu |
|---|---|---|
| `SAMPLE PROFILE · 15 SOURCED DOORS` | `Exploring with a sample profile` | Pack count và source metadata |
| `Source reviewed on 2026-10-21 · within Porta's 90-day review window` mỗi card | `Checked 21 Oct` *chỉ khi hữu ích* | Full source/provenance; stale evaluation không đồng nghĩa official currency |
| `ASSESSSED RULES PASSED` | `Known checks passed` / `Checks met` nếu logic cho phép | Định nghĩa đầy đủ về coverage |
| `20.0 years` và `18.0 years` | `Age 20 · Minimum 18` | Exact typed comparator |
| `Actual: Needs information → Simulated: Needs information` | `1 check improved · Other checks still pending` | Overall state before/after ngay dưới |
| `Would satisfy this programme-specific check if confirmed` | `Could meet the attendance check` | Scope/exact assertion, hypothetical |
| `Time estimate · Porta assumption · Impact basis ...` | `Estimate · unconfirmed` hoặc `Time unknown` | Estimate source, uncertainty, impact method |
| `No safe profile change can be inferred yet` | `No reliable action yet` | Vì sao không có action và nên xác minh ở đâu |

Chú ý: Không tự thay “Needs information” bằng “Almost eligible”, không dùng `Open`, `Unlocked`, `Eligible` nếu chỉ pass một rule.

---

## 2. Visual identity: "Editorial Thresholds"

### 2.1 Bốn hình thái nhận diện

1. **Arch (cửa vòm):** biểu tượng cho một cơ hội. Dùng làm crop/frame/marker/chuyển cảnh, không biến mọi card thành hình vòm cỡ lớn.
2. **Threshold (ngưỡng):** một line/mark cụ thể trên indicator; ví dụ IELTS threshold. Đánh dấu điều kiện chứ không dự đoán trúng tuyển.
3. **Path (đường đi):** kết nối *điều kiện → hành động → tác động*, dùng mini-stepper hoặc connector line.
4. **Ripple (lan tỏa):** một fact/action liên quan tới nhiều cơ hội; chỉ kết nối những opportunity được engine trả về, tránh demo ảo.

**Phép ẩn dụ cần trung thực:** cửa "sáng" có thể nghĩa *một điều kiện được cải thiện*, **không** được hiểu là chương trình đã chấp nhận người dùng. Dùng nhãn `Rule met` hoặc `Progress`, không ghi `Unlocked` với rule-level improvement.

### 2.2 Màu

Giữ token hiện có thay vì rebrand:
- Dark background theo repo report `#17151B`, dark surface `#211E25`, body `#F3EFF5`, brand lavender `#B9ACEA`.
- Light-first warm ivory cũng phải được hỗ trợ theo token; tránh các màn chỉ đẹp trong dark mode.
- Brand lavender: primary CTA, navigation selection, path highlight.
- Teal: **confirmed rule-level progress** hoặc success có định nghĩa rõ; không tô teal cả `Needs information`.
- Ochre/amber: cần hành động/xác minh.
- Muted neutrals: unknown, inactive, archived.
- Error/rose: lỗi hoặc điều kiện được đánh giá fail, không biến thiếu dữ liệu thành lỗi.
- Source freshness riêng với eligibility state: một source đã kiểm tra gần đây vẫn có thể có deadline TBA.

**Phân cấp diện tích màu:** phần lớn neutral; một điểm nhấn lavender mỗi viewport; status accents vừa đủ. Không sử dụng gradient tím toàn phần trên mọi card.

### 2.3 Typography

- **Newsreader serif** cho hero headline (onboarding, top-level title), tên opportunity được spotlight, và single-result headline khi thực sự là điểm nhấn.
- **Manrope sans** cho row, form, filters, status, receipt, CTA và mô tả.
- Hạn chế serif đồng thời ở tên tất cả item, title section và milestone trên cùng màn.
- Màn list desktop ưu tiên tốc độ scan: dòng tên khoảng 18–22sp (đề xuất, adaptive), metadata 12–14sp; không thu nhỏ xuống dưới mức accessibility để nhét nội dung.
- Content width khác nhau theo tác vụ: essay/onboarding hẹp; form vừa; desktop list/detail chia cột khi hợp lý. Tránh CTA full-screen width dưới content max-width.

### 2.4 Shape, icon & surface

- Round-corner tiêu chuẩn theo design system (ví dụ 12/16/20dp theo vai trò), không tăng radius vô hạn.
- Arch shape là **signature art asset**, không buộc mọi surface phải arch.
- Icon style thống nhất: một bộ line icon, stroke/size tương đồng, dùng icon đúng ngữ nghĩa.
- Loại icon status: pass/check, unknown/question, action/route, deadline/calendar, source/book-open, bookmark; luôn kèm accessible text.
- Không dùng ảnh thực địa/cờ/logo làm bằng chứng eligibility. Các asset đó chỉ là identity cue.

---

## 3. Danh mục hình minh họa có thể thiết kế

Đây là *brief* cho designer hoặc dùng làm vector dựng bằng Compose/Skia/SVG. Không cần bức nào phải làm bằng AI.

| Asset ID | Tên | Dùng ở đâu | Composition | Interaction | Priority |
|---|---|---|---|---|---|
| **A01** | `Threshold Doors` | Onboarding step 2/3 | 1 đường input ở dưới → 3–4 cổng vòm nhỏ ở trên | Đèn/outline đổi nhẹ khi threshold thật sự đạt | P1 |
| **A02** | `Personal Atlas` | Profile empty state | 3–5 vòm/nút thưa + một đường mảnh chưa hoàn thiện | Đường lộ dần khi có facts; không tuyên bố unlock | P1 |
| **A03** | `No Path Yet` | No actionable step | Một đường dừng tại nút hỏi, tông trung tính | Không cần animation lặp vô hạn | P2 |
| **A04** | `Saved Doors` | Saved empty state | 1–2 arch đơn giản và bookmark | Nhẹ khi lưu item đầu tiên | P2 |
| **A05** | `Action Ripple` | Next moves/What-if | Một action node bên trái + những affected nodes bên phải | Nối theo đúng số ID từ engine | **P1 quan trọng** |
| **A06** | `Plan Milestone` | Active plan/completion | Path segment + marker trạng thái | Tiến tới confirmed actual update, không ăn mừng khi chỉ planned | P1 |
| **A07** | `Pack Refresh` | Update/load state | Một bộ giấy/card và vòng refresh tối giản | Không animation liên tục không cần thiết | P2 |

**Art specs đề xuất**
- Hình 2D vector, không đổ bóng 3D, không photoreal, không gradient mesh sặc sỡ.
- Nét 1.5–2dp ở viewport mobile mặc định; scale theo density.
- Phối lavender + neutral; dùng teal/amber khi mô tả đúng semantic.
- Số thành phần ít, hình học nhất quán, phù hợp cả light/dark mode.
- Có static fallback, reduced-motion fallback và screen reader alternative.
- Minh họa thương hiệu không dùng logo nhà cung cấp nếu không có quyền hoặc file hợp lệ.
- Không xuất/nhúng font file ngoài repo để phân phối.

### Asset nào không nên làm
- Nhân vật người cầm laptop, mũ tốt nghiệp bay, mascot cartoon.
- Background illustration có độ tương phản cao phía sau văn bản.
- Ảnh từng học bổng bắt buộc phải có: data có thể thiếu logo và content không đồng bộ.
- Ảnh cờ quốc gia làm đại diện duy nhất cho cơ hội global hoặc đa quốc gia.
- Minh họa cửa mở 100% khi chỉ một requirement pass.

---

## 4. Kế hoạch **từng màn**, ưu tiên rõ ràng

### 4.1 Onboarding step 1 — Focus

**Hôm nay:** Option cards đã gọn hơn bản đầu; có chọn Scholarships/Internships/Exchange.
**Mục tiêu:** Chọn hướng đi trong một nhịp, không ép cuộn để nhìn đủ option.
- [P1] Giữ 3 option hàng gọn, icon category dễ nhận; selected state dùng nền/border nhẹ.
- [P1] Một câu ngắn `Choose what you want to explore first.`; bỏ giải thích phòng thủ.
- [P2] Ở desktop, thêm một arch hero nhỏ trên khoảng trống *nếu thật sự cải thiện nhận diện*, không che card.
- [KEEP] Không thêm illustration lớn làm chậm first action.
- **Interaction:** chọn xong thấy thay đổi rõ. Nếu lựa chọn chỉ sắp xếp demo, không gọi đây là kết quả cá nhân hóa thật.
- **Success:** mọi option hiển thị mobile phổ biến không phải cuộn quá nhiều; CTA phù hợp với action.

### 4.2 Onboarding step 2 — IELTS interaction

**Hôm nay:** Slider 6.5→7.0 và progress text.
**Mục tiêu:** Visual trực tiếp nối fact với opportunities.
- [P1] Slider kèm vertical threshold mark đúng value; min, max, target.
- [P1] Dưới slider là 4 mini arch/node (sample data) ngay tại chỗ thay vì chỉ bốn dấu tròn vô danh.
- [P1] Khi crossing 7.0, highlight đúng 4 sample IELTS rules; status label là `IELTS check met`, không `Eligible`.
- [P1] Distinguish slider/action focus và replay.
- [P2] Stagger 100–220ms tổng hợp, keyboard/screen reader tương đương.
- **Success:** người dùng thấy quan hệ nhân quả mà không phải đọc bốn tên dài hoặc paragraph.

### 4.3 Onboarding step 3 — Before/after payoff

- [P1] Before/after block gọn trên cùng; chỉ số `4 IELTS checks changed` đúng theo demo model.
- [P1] Bốn sample arches hiển thị card compact tên ngắn và status.
- [P1] CTA tạo profile/công đoạn tiếp theo đúng đích.
- [P2] Motion reveal arch states 1 lần, không lặp.
- **Warning:** Sample synthetic không đồng nghĩa real pack sẽ cho ra cross-door gain tương tự.

### 4.4 Onboarding step 4 — Conversion

- [P1] Hai lựa chọn `Create your profile` / `Keep exploring`, phân cấp primary/secondary.
- [P1] Thêm A02 *nhỏ* bên cạnh hero text nếu có khoảng trống; mobile có thể bỏ.
- [P1] Demo disclosure một dòng; không lặp 3 nơi.
- [P2] Path từ demo → personal insight với transition tinh tế.

### 4.5 Doors — Listing

**Ảnh 26/10:** header `0 assessed... 4 needs action 10 needs information 1 archived`; ~8 filter chips trên 2 hàng; cards lặp đoạn source review.
- [P0] Card tier Glance chỉ còn: category/provider cue, title, 1 status, *one next meaningful check*, known deadline/missing deadline signal, bookmark.
- [P0] Bỏ paragraph official rule dài khỏi list; giữ trong detail.
- [P0] Chỉ metadata source tóm tắt khi ảnh hưởng quyết định (outdated/recheck required). `Checked Oct 21` nằm trong Evidence hoặc tooltip.
- [P1] Filter chính `All`, `Needs action`, `Needs info`, `Saved`; group `Archived/Closed/Other` trong More filter. Không xóa khả năng truy cập.
- [P1] Category glyph monoline / optional verified provider logo + fallback; không bắt buộc logo mọi card.
- [P1] Category/filter có động cơ rõ: discovery nhanh, tên dài wrap tối đa hợp lý, không crop status.
- [P2] Desktop có thể triển khai list + detail panel; mobile card list một cột.
- **Important:** sample mode nói sample; không viết `your current profile` khi không có profile thật.
- **Acceptance:** trong viewport mobile, nhiều hơn trước về số cơ hội có thể scan; đọc card không cần scroll tới source footnote; source detail vẫn truy cập được.

**Sketch (layout meaning, not pixel mock):**
```
[Category icon] Kotlin Student Coding Competition      [☆]
Kotlin Foundation · Contest
[Needs information]   [Dec 7]
? Confirm enrollment eligibility                 [View →]
```

### 4.6 Door details — Eligibility checklist (P0 UI)

**Ảnh 26/10:** rất dài: conclusion, no action, no next move, manual review, `Your current value`, `Published check`, source note, soft selection factors.
- [P0] Hero: title, category, deadline/source status. Không dùng heading `"Opportunity details"` chiếm hierarchy lớn hơn tên chương trình.
- [P0] Summary strip: `Known checks met: N`, `Needs info: M`, `Needs action: K` nếu các con số chắc chắn; và short overall state.
- [P0] Groups: `Met`, `Needs action`, `Needs information`, `Other selection factors` (collapsible).
- [P0] Mỗi requirement dạng row: icon + 1-line name + 1-line current→expected; expand để xem exact source.
- [P0] Dùng `Not assessed` khi parser không đánh giá, không hiển thị `Not provided` cho manual check không ánh xạ với input.
- [P1] `Why this status?` và `Official sources` trong disclosure, source link actionable.
- [P1] Contextual CTA: `Check what to verify` hoặc `Preview relevant step`, chỉ nếu có mapping action thực.
- [P1] Nếu không có action, dùng one-line empty guidance thay ba section lặp `no safe change`.
- [P2] Visual arch-progress accent mảnh ở top nhưng **không** hiển thị % eligibility giả.
- **Acceptance:** default view có thể trả lời "what's met / what needs attention / what next?" mà không đọc full receipts; full receipts unchanged upon expand.

**Sketch:**
```
Kotlin Student Coding Competition
[Needs source review]    Deadline: Dec 7

Your checks
 ✓ Age                     20 / minimum 18
 ✓ Current enrollment      Rule branch met
 ? Full-time / recent      Confirm official conditions

[Verify unresolved check]   [Official source ↗]
  ▸ Full rule & evidence
```

### 4.7 Next moves — Recommendation first, then alternatives

**Ảnh 26/10:** recommendation-first thành công; còn action dài, assumption/impact/cost nhiều dòng, repeated single-door alternatives.
- [P0] Header giới hạn 1 dòng; recommended card: type (`Verify/Prepare/Improve`), action name, one concrete intended effect, affected count, 1 primary CTA.
- [P0] `1 door affected` phải có ý nghĩa: `1 requirement potentially addressed` + `CERN` tùy trường hợp; không phóng đại.
- [P1] Dùng A05 Action Ripple chỉ khi action tác động nhiều opportunity; 1 door thì dùng **single cause-effect link** hoặc checklist, tránh biểu đồ giả drama.
- [P1] Alternatives group theo *action family* để tránh 4 card title y hệt; vẫn giữ mỗi per-programme scoped assertion độc lập. Nhóm có nút xem từng chương trình.
- [P1] Estimate chỉ hiển thị trong compact row nếu thực sự có giá trị/căn cứ; provenance ở expansion `Why this?`.
- [P1] Ưu tiên recommendation có ích thực sự hơn chỉ sort theo counts; nếu engine chỉ có verification actions, headline `Suggested verification` thay cho `Best next move`.
- [P2] Pareto graphic chỉ hiện khi đủ phương án khác biệt; nếu không, `Compare options` list/table.
- **Acceptance:** ít text hơn đáng kể; không merge rule scope; người dùng phân biệt được verify vs improve; no fabricated 14-day confidence.

### 4.8 Planning limits

- [P1] Collapsed summary là mặc định; `Adjust` mở sheet/panel.
- [P1] Mức effort/cost có tooltip/definition ngắn (tránh suy diễn `Low = 2h/week` khi engine chưa định nghĩa).
- [P1] Khi filter thay đổi, show count delta nếu có; nếu không, không cần animation đếm giả.
- [P1] `Any` không đồng nghĩa estimate chính xác; label `Unknown estimates included` khi có.
- [P2] Desktop có right-side inspector panel; mobile bottom sheet/full screen.
- **Acceptance:** người dùng không phải đọc 3 đoạn giải thích dài trước khi tương tác.

### 4.9 What-if — Core signature moment

**Ảnh 26/10:** accurate rule-level explanation, nhưng nhiều text và repeated `Needs information → Needs information`.
- [P0] Above fold summary theo thứ tự: `Impact`, `Changed check`, `Still unresolved`. Keep clear `Simulation · does not change your profile`.
- [P0] Dùng row/mini diagram:
```
[Action] Confirm programme availability
    │
    ├── ✓ Attendance check      Unknown → Would meet
    └── ? Other conditions      Still need review
```
- [P0] Overall conclusion có thể không đổi; phải hiển thị thông báo ngắn `Opportunity still needs information` ngay cạnh changed rule, không ẩn.
- [P1] A05 ripple nếu nhiều affected door IDs thật; 1 door thì một connector animation ngắn.
- [P1] `View exact checks` chứa ALL: source notes, fingerprints nếu người dùng cần, full rule detail, assumptions. Never delete.
- [P1] CTA bottom `Add to plan` (real profile) hoặc `Create profile to save` (sample mode); secondary `Discard`.
- [P1] `Unchanged doors (14)` dưới disclosure, không paragraph chiếm chỗ.
- [P2] Subtle 150–220ms reveal after simulation computed, static under reduce motion.
- **Acceptance:** no false eligible/unlocked; counts tie to scenario engine; keyboard and screen reader announces major delta; one-glance summary understandable.

### 4.10 Active plan + actual update (chưa có screenshot mới)

- [P1] Vertical timeline: Planned → Working on it → Completed → Confirm actual.
- [P1] Action checklist vs confirmed evidence status riêng. `Completed` does NOT auto-PASS any rule.
- [P1] One active plan + history visual. Dựa vào nguồn code report cũ, `replaceForProfile` xóa plan cũ: nếu chưa được wave mới sửa phải kiểm tra trước khi thiết kế history.
- [P2] A06 path illustration cho empty/in progress; progress không dùng % nếu không có breakdown.
- **Acceptance:** state names/haptics/animation phân biệt rõ action completion và factual confirmation.

### 4.11 Profile empty state + edit profile

- [P1] Empty state A02 Personal Atlas, CTA primary, lợi ích một câu: `Understand the checks that apply to you`.
- [P1] Progressive profiling: Essentials trước; các fact khác contextually theo rule cần dùng.
- [P1] Show completeness in terms of *known facts* hoặc `x fields provided`, không score năng lực/người.
- [P1] Input labels cố định, GPA scale, validation, nationality selector; age/year semantics đúng theo source if computed.
- [P1] Desktop form có max width, không full-screen edit field; mobile vẫn scroll thoải mái.
- [P2] Subtle path node update sau save; giữ quyền riêng tư rõ ràng nhưng không lặp `local` ở 5 vị trí.
- **Acceptance:** Save enabled/disabled reason accessible; optional unknown ≠ fail; no unverified profile data relabeled as externally verified.

### 4.12 Saved, no results, loading, error, pack inspector

| State/screen | Visual | Primary copy/action | Avoid |
|---|---|---|---|
| Saved empty | A04 bookmark arch | `Save opportunities to compare later` | Giả định người dùng chưa quan tâm |
| Filter no results | icon filter + neutral path | `No matches for these filters · Clear filters` | Hình door khóa đỏ |
| No eligible recommendations | A03 question path | `No reliable next move found yet · Review missing details` | Fake recommendation |
| Loading catalog | skeleton theo card shape | `Checking locally saved sources…` nếu đúng | Endless rotating marketing hero |
| Source stale | amber source cue | `Source may need rechecking` + official link | `Verified` |
| Import error | plain structured message | Explain which record/reason failed and retain current pack | Decorative success |
| Pack inspector | restrained data table + source icons | Source review/version/search | Illustration decorative over diagnostics |

Pack Inspector là giao diện chuyên sâu: cho phép nhiều text hơn vì đúng nhu cầu, nhưng có hierarchy và search. Không cần art illustration ở trang kỹ thuật.

---

## 5. Design system components cần thiết kế và tái sử dụng

| Component | Inputs/data contract | Tầng hiển thị mặc định | Interaction/accessibility |
|---|---|---|---|
| `PortaOpportunityCard` | id/title/provider/category/status/deadline/primaryUnresolvedCheck/saved | icon/title/status/1-line reason/deadline | whole card click; bookmark separate |
| `StatusLabel` | typed status + explainability | icon + text + semantic color | status không chỉ bằng màu |
| `RequirementRow` | rule name/result/current/expected/source | icon/title/value, short | expand receipts |
| `EligibilityOverview` | evaluated hard results, unknown/manual totals | grouped counts, no % | open grouped details |
| `SourceDisclosure` | URL/checked date/cycle/reviewer/confidence boundary | link + brief freshness cue | opens official source |
| `ActionTypeLabel` | VERIFY / PREPARE / IMPROVE | mini badge | semantic label |
| `NextMoveHero` | action target, scope, affected ids, time estimate with source, intended impact | 1 core promise + primary CTA | precise output based on model |
| `ActionRippleDiagram` | source action id; per-door results | action node → computed affected nodes | optional static/screen reader description |
| `ScenarioDeltaPanel` | before/after rule status + overall state + unresolved list | 3 layers impact/change/remain | expandable evidence |
| `EstimateBadge` | value/range/unknown/basis/confidence | compact only if useful | accessible uncertainty |
| `PlanPath` | plan steps/status/confirmation | 3–4 segments | reduced motion/keyboard |
| `PortaEmptyState` | kind, short copy, action, art asset | illustration + one meaningful action | static fallback |

### UI state contract (không nhất thiết đổi domain model)
- UI projection nên phân biệt `KNOWN_MET`, `KNOWN_UNMET`, `UNKNOWN_FACT`, `MANUAL_REVIEW`, `CLOSED`, `ARCHIVED`, `TIMING_UNVERIFIED`.
- `Hypothetical` là modifier trên evaluated fact, không phải một `PASS` thực.
- UI không tự tính “số doors mở” từ animation, chỉ đọc evaluator/scenario result.
- Source badges không tác động rule truth. `checked recently` ≠ `valid now`.

### Quy tắc icon và biểu tượng
- Mỗi asset có intrinsic meaning; không trộn category với status.
- Badge/action icon 16–20dp, touch target riêng đủ ≥48dp theo intent dự án.
- Category icons: scholarship → graduation, internship → briefcase, student contest → code/trophy, exchange → globe; icon category không thay thế title.
- Provider logos chỉ nếu hợp lệ và được đưa vào asset có quyền sử dụng; fallback text monogram trong vòng tròn/arch.
- Không dùng emoji lẫn native icons trong sản phẩm thật.

---

## 6. Responsive spec & UI rhythm

### Mobile
- Bottom nav ba tab, one-column cards.
- Root content single vertical scroll; sticky CTA chỉ cho các bước cần commit.
- Density vừa: 2–3 concise opportunity cards thường nên scan được mỗi màn thông dụng tùy font scale.
- Không đặt 8 filter chips hai hàng thường trực nếu có More.
- `What-if` mặc định Impact → Changed → Remaining, evidence collapsed.
- Đảm bảo keyboard không che Save/CTA, Android back xử lý hợp lý.

### Tablet
- Content max-width trung gian; detail panel hoặc two-column ở nơi thực sự hỗ trợ comparison.
- Không tự tăng font line-length vô hạn.

### Desktop (theo context adaptive rail >=840dp; Frontier split >=900dp)
- Bố trí coherent rail + list/detail; ưu tiên nội dung chính hơn giant empty margins.
- Input form medium width. Onboarding medium width. Sidebar đủ tên và hit target.
- Keyboard navigation/hover/focus visible; card entire row click, bookmark riêng.
- Không kéo một CTA full width cửa sổ nếu form content chỉ max 620dp.
- Resize từ wide về compact không làm mất selection, check text wrapping.

### Nhịp bố cục
- Ở mỗi viewport có một vùng "tập trung": hero result OR visual diagram OR action.
- Không cần card border cho mọi section; dùng group headings, separators, whitespace.
- Bỏ nested cards lặp khi có thể thay bằng row/divider.
- Không đồng thời có 3 button primary color trong một viewport.
- Dùng một accent hình học (path/arch) tối đa khi có cơ hội để brand nhận diện.

---

## 7. Motion system

**Tuyên ngôn:** Animate the change in understanding, not every screen.

| Event | Motion đề xuất | Duration tham khảo | Reduced motion |
|---|---|---:|---|
| Select category | state color/border fade | 120–160ms | instant |
| Filter Doors | small content fade/layout shift | 150–220ms | instant |
| Expand evidence | section reveal/collapse | 160–220ms | instant |
| What-if calculate | numerical and connector reveal once | 180–280ms | static diagram |
| Rule changes | highlight row and changed-status badge | 140–220ms | change text |
| Sample door ripple | 3–4 nodes stagger total ≤450ms | 180–450ms | instant final state |
| Save opportunity | bookmark fill feedback | 120–180ms | icon state |
| Plan completion | path marker state confirmation | 200–300ms | status text |

- Không làm perpetual ambient animation.
- Không che user action bằng animation dài; interactive states always usable.
- Không dùng confetti khi rule chưa xác minh; no “celebration” for simply viewing a scenario.
- Đảm bảo screen reader đọc status change một lần, không đọc 14 line raw receipts.
- Desktop hover/focus ≠ Android haptics; motion/haptics được opt-out đúng nền tảng.

---

## 8. Illustrations production pipeline

### Hướng 1 — vector dựng bằng Compose/Canvas
Ưu tiên cho: arch/path/ripple/threshold. Ưu điểm: tự scale, cùng tokens, animation, KMP/shared. Nhược: cần implementation + QA.

### Hướng 2 — SVG/vector asset dựng bởi designer
Ưu tiên cho: empty states A02–A04; asset gốc phải có quyền dùng, phiên bản light/dark nếu cần. Tránh bitmap scale kém.

### Hướng 3 — raster art nếu thật sự cần
Chỉ dùng khi artist-led illustration là giá trị riêng; cần export @ density và cả light/dark. Không dùng ảnh AI với ký tự/nhãn quan trọng bên trong vì không localizable, không accessible.

### Asset manifest khuyến nghị
```
designsystem/assets/porta/
  threshold_arch.*
  personal_atlas.*
  no_path.*
  saved_doors.*
  action_ripple.*
  plan_path.*
```
`.*` là format được repo hỗ trợ (không bắt Codex tự đoán SVG parser). Tên chỉ là đề xuất.

### Quy trình duyệt
1. Figma/Vector thumbnail grayscale.
2. Light & dark contrast.
3. Android small/large font.
4. Desktop resized, focus states.
5. Animation optional, reduced-motion version.
6. Mark which signals are **identity**, which are **data**; cấm asset bịa số liệu.

---

## 9. Các anti-pattern cần cấm trong wave visual

1. Thêm hero illustration vào **mọi** opportunity.
2. Thêm nhiều chữ để “giải thích cho đúng” ở tầng Glance.
3. Tạo progress bar “80% eligible” từ số rule pass chia total.
4. Vẽ 4 arches mở cho 4 record thật nếu engine chỉ báo 1 scoped improvement.
5. Tô green/teal cả Door khi chỉ một leaf check pass.
6. Làm button chữ nhỏ hoặc contrast thấp cho giao diện đẹp hơn.
7. Đặt full raw URL dài trong list card.
8. Gộp four `OpportunityRuleTarget` thành one global assertion chỉ để demo đẹp.
9. Giấu timing-unverified vào tầng Evidence nếu người dùng có thể hiểu lầm feasible.
10. Dùng logo / flag / bản đồ khi không có nguồn đúng hoặc chưa có quyền.
11. Rebrand Newsreader/Manrope và toàn bộ palette một cách vô cớ.
12. Thêm chatbot/AI content chỉ để giao diện có điểm nhấn.

---

## 10. Roadmap theo wave (không cần làm hết một lần)

### Wave V0 — Baseline + contract (chặn regressions)
- Screenshot baseline Android compact + desktop (light/dark).
- Audit strings, duplications, and view hierarchy.
- Check three truth contracts: rule status, what-if hypothetical, scoped actions.
- Test existing flows before implementing visual changes.
- **Deliverable:** `VISUAL_REFACTOR_PLAN.md`, snapshot/test references.

### Wave V1 — Text reduction & hierarchy
- Doors compact cards, filters, source disclosure.
- Details checklist + expandable exact receipts.
- Next moves compact recommendation/alternatives.
- What-if three-layer view.
- **Pass gate:** core facts still reachable and conveyed correctly; fewer paragraphs in default views; no empty “fake” spaces caused by removal.

### Wave V2 — Branded visual explanation
- Mini category glyphs and fallback provider identities.
- A01/A05: Threshold/Ripple visual from real computed state (synthetic onboarding is clearly synthetic).
- A02–A04: 2–3 limited-context empty-state illustrations.
- Design tokens and component documentation.
- **Pass gate:** every illustration encodes a meaningful state or identity and has text alternative.

### Wave V3 — Interaction + responsive
- Motion system, reduced-motion behavior, focus/keyboard.
- Android and desktop adaptive rendering.
- Sticky CTA, dialogs/sheets, state persistence after resizing/tab switching.
- **Pass gate:** no clipped content or keyboard overlap; motion doesn't change semantics.

### Wave V4 — Quality and usability
- Accessibility contrast + large font + TalkBack/desktop keyboard.
- Synthetic and real scenarios, no stale-data misclaims.
- Mini usability check: ask testers after 5 seconds “What's happening? What can you do next? What remains unknown?”
- **Pass gate:** no critical semantic regressions; user testing claims only if done.

**Không biến thành một pull request khổng lồ.** V1/2/3 là các PR/commit nhỏ có ảnh before/after.

---

## 11. Test matrix and acceptance criteria

### Functional truth
- [ ] `UNKNOWN`/`REVIEW` không biến thành `PASS` vì visual.
- [ ] What-if does not mutate actual profile; add plan alone doesn't confirm fact.
- [ ] Detailed rule receipts và official source luôn truy cập được.
- [ ] Per-programme scoped availability chỉ ảnh hưởng đúng programme.
- [ ] Affected count đúng engine output; no invented degree/GPA/IELTS source claims.
- [ ] Archived/closed/timing unknown không bị ẩn khỏi sản phẩm.

### Interaction
- [ ] Bookmark icon/state rõ, touch target ≥48dp, independent action.
- [ ] Compact filter trên mobile có đường truy cập all states.
- [ ] Expand/collapse giữ selection và scroll position đúng.
- [ ] Saved/sample/real modes được label đúng.
- [ ] Keyboard focus hiển thị rõ; screen reader có text thay cho visual diagrams.
- [ ] Reduced motion disables nonessential choreography.

### Visual
- [ ] Newsreader/Manrope và brand palette preserved (không rebrand).
- [ ] Không có các đoạn source/timestamp lặp trên every card.
- [ ] Default What-if tập trung impact/change/remain.
- [ ] Default details ưu tiên grouped check rows, không full plain-text report.
- [ ] Bất kỳ illustration nào đều có art brief / semantic meaning / static fallback.
- [ ] Không có layout overflow ở compact Android hoặc resized Desktop.

### Usability targets (mục tiêu test, không phải dữ liệu có sẵn)
- [ ] 5 giây người mới chỉ ra được status chính và next tap ở một card.
- [ ] 10 giây người mới chỉ ra check đổi trong What-if và biết đó là hypothetical.
- [ ] Testers không nhầm “rule met” với “admission awarded”.
- [ ] Người dùng có thể tìm full official source ≤2 tương tác từ details.
- [ ] Không ai cần đọc raw source note để hiểu action category.

---

## 12. Mini design exercise: 3 key components

### Concept A: compact opportunity card
```
┌────────────────────────────────────────────────────────┐
│  ◇ Kotlin Student Coding Competition          ☆        │
│  Kotlin Foundation · Contest                            │
│  [Needs information]      [Due Dec 7]                   │
│  ? Confirm enrolment conditions                 →       │
└────────────────────────────────────────────────────────┘
```
**Lợi ích:** loại card text + source footnotes; dễ scan.

### Concept B: Door detail checklist
```
Kotlin Student Coding Competition          [Needs review]
─────────────────────────────────────────────────────────
Your checks
✓ Age                         20 · minimum 18
✓ Current enrolment            Encoded branch met
? Full-time/recent enrolment   Confirm official conditions
─────────────────────────────────────────────────────────
[Check official requirements →]
▸ Why this status? · Exact checks and sources
```
**Lợi ích:** người dùng biết gì/không biết gì.

### Concept C: What-if visual cause → effect
```
SIMULATION · Profile unchanged
[Verify CERN attendance]
         │
         ├────  ✓ Programme availability
         │       Not provided → Would meet
         │
         └────  ? Further criteria & deadline
                 Still require confirmation

[Create profile to save]    [Exact rule details ▾]
```
**Lợi ích:** tác động có thể nhìn thấy, không bị ngập văn bản.

---

## 13. Scope / decision record

**Nên làm ngay:** V1 rồi A05/A02 selective in V2.
**Không làm ngay:** 7 illustration lớn, provider images cho 15 cards, full animated constellation, gamified door unlocking, global visual rebrand, arbitrary recommender UI with fabricated impact.
**Định vị:** Visual system nên làm *opportunity-to-action* đáng tin và thú vị, nhưng không che sự thật rằng nguồn dữ liệu còn thiếu/đã cũ.

### Cần xác minh khi Codex triển khai
- Current source layout có khác screenshot wave 4 hay không.
- Release target/light-theme hiện hoạt động đến đâu.
- Remaining-gaps/exact-check projection API đã có hay chưa.
- Nếu constraints/duration không chắc, UI data type có hiện UNKNOWN/range chưa.
- Personal profile vs sample mode saved state và CTA semantics.
- Theme/accessibility/reduced motion được triển khai ở đâu.
- All four scoped availability actions still exist independent by ID after grouping visually.

---

## 14. Final design principle

> **Less reading, more seeing. More seeing, without less truth.**

Porta không cần trở nên ồn ào. Cái nó cần là một ngôn ngữ hình ảnh riêng, một nhịp UI đa dạng, và *một bước tiến có ý nghĩa* được hiển thị rõ hơn rất nhiều so với metadata và lời giải thích kỹ thuật.

Mọi minh họa, trạng thái động và hiệu ứng nên khiến người dùng hiểu một hành động tác động tới những cơ hội nào, điều kiện nào thay đổi, và điều kiện nào vẫn chưa biết.
