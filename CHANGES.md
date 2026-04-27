# BNR Stock Management — Detailed Changes & Implementation Notes

This document provides an in-depth explanation of every major change made after the initial project setup. Each section explains the problem that existed, the reasoning behind the solution, and a detailed walkthrough of every file that was touched and why.

---

## 1. Fixed Duplicate Lombok Dependency

### Background
Lombok is a Java library that eliminates boilerplate code. Instead of writing `getters`, `setters`, `constructors`, and `toString()` methods manually on every entity class, you just annotate the class with `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and Lombok generates all of that at compile time.

### The Problem
In `pom.xml`, Lombok appeared twice:

```xml
<!-- First entry — correct -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- Second entry — problematic -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

Maven raised a warning: _"Overrides version defined in DependencyManagement. The managed version is 1.18.30"_.

### Why This Is a Problem
Spring Boot uses a parent POM (`spring-boot-starter-parent`) that acts as a central version manager for all common dependencies including Lombok. When you explicitly set a version in your own `pom.xml`, you override that central management. This can cause version mismatches, unexpected behavior during builds, and makes the project harder to upgrade in the future.

### The Fix
The second entry was removed entirely. The first entry — which has no version and uses `<optional>true</optional>` — is the correct pattern. `<optional>true</optional>` means Lombok is only needed at compile time and will not be bundled into the final JAR, which is exactly what you want since Lombok's job is done before the application runs.

---

## 2. Request Rejection Reason — Dropdown Modal

### Background
In a stock management workflow, when an approver rejects a request, the requester needs to understand why. Without a reason, the requester has no way to correct their request or understand the policy that was applied.

### The Problem
The original `rejectRequest()` function in `ApproverRequestsView.vue` called `updateRequestStatus(id, 'Rejected')` directly when the Reject button was clicked — no confirmation, no reason, nothing saved to the database.

### The Solution
A full rejection workflow was implemented across the backend and frontend:

---

#### Backend — `Request.java` (Entity)

The `Request` entity already had a `rejectionReason` field:

```java
@Column
private String rejectionReason;
```

The `@Column` annotation with no constraints makes it nullable — meaning it is only populated when a request is rejected, and stays `null` for approved or pending requests. Hibernate automatically manages this column in the `requests` table via `ddl-auto=update`, so no SQL migration script was needed.

---

#### Backend — `RequestController.java`

The `PUT /requests/{id}/status` endpoint uses an inner class `StatusUpdate` to receive the request body:

```java
public static class StatusUpdate {
    public Request.Status status;
    public String rejectionReason;
}
```

The endpoint logic checks whether a reason was provided before setting it:

```java
req.setStatus(body.status);
if (body.rejectionReason != null) req.setRejectionReason(body.rejectionReason);
return requestRepository.save(req);
```

This means the same endpoint handles both approvals (no reason) and rejections (with reason) — no separate endpoint was needed.

---

#### Frontend — `stores/requests.js`

The `updateRequestStatus` function was updated to accept an optional third argument:

```js
async function updateRequestStatus(id, newStatus, rejectionReason = null) {
    const payload = { status: newStatus }
    if (rejectionReason) payload.rejectionReason = rejectionReason
    const response = await api.put(`/requests/${id}/status`, payload)
    ...
}
```

The `rejectionReason = null` default means existing calls that don't pass a reason (like approvals) continue to work without any changes.

---

#### Frontend — `ApproverRequestsView.vue`

Three new reactive variables were added:

```js
const showRejectModal = ref(false)   // controls modal visibility
const rejectTargetId = ref(null)     // stores which request is being rejected
const selectedReason = ref('')       // tracks dropdown selection
const customReason = ref('')         // tracks free-text input when "Other" is selected
```

The Reject button now calls `openRejectModal(req.id)` which stores the request ID and resets the form state before showing the modal.

The modal itself has three key behaviors:
 1. A `<select>` dropdown with 7 predefined reasons covering the most common rejection scenarios
2. A conditional free-text `<input>` that only appears when "Other" is selected — implemented with `v-if="selectedReason === 'Other'"`
3. The Confirm button uses `:disabled` binding — it stays disabled until either a predefined reason is chosen, or if "Other" is selected, until the custom text field is not empty

```html
<button @click="confirmReject" :disabled="!selectedReason || (selectedReason === 'Other' && !customReason.trim())">
  Confirm Rejection
</button>
```

`confirmReject()` resolves the final reason and calls the store:

```js
const reason = selectedReason.value === 'Other' ? customReason.value.trim() : selectedReason.value
requestsStore.updateRequestStatus(rejectTargetId.value, 'Rejected', reason)
```

---

## 3. Currency Toggle — RWF / USD / EUR

### Background
All prices in the database are stored in Rwandan Francs (RWF) because that is BNR's operating currency. However, for reporting and comparison purposes, it is useful to view those values in USD or EUR. Exchange rates change daily, so hardcoding conversion values would quickly become inaccurate.

### The Problem
The original inventory and dashboard views displayed all prices with a hardcoded `$` symbol and no conversion logic. The values were actually stored in RWF but displayed as if they were USD, which was misleading.

### The Solution
A shared Pinia currency store was built that fetches live exchange rates and exposes a `convert()` function. Both admin views use this same store, so switching currency anywhere updates everywhere instantly.

---

#### Step 1 — `stores/currency.js` (New File)

This store is the single source of truth for currency state across the entire application.

**Live rate fetching:**
```js
const res = await fetch('https://open.er-api.com/v6/latest/RWF')
const data = await res.json()
if (data.result === 'success') {
    rates.value = { RWF: 1, USD: data.rates.USD, EUR: data.rates.EUR }
    lastUpdated.value = new Date(data.time_last_update_utc).toLocaleDateString()
}
```

The API `open.er-api.com` is free, requires no API key, and returns rates with RWF as the base currency. This means `rates.USD` is directly how many USD equal 1 RWF — exactly what is needed for conversion.

**Fallback rates:**
```js
const rates = ref({ RWF: 1, USD: 0.00072, EUR: 0.00066 })
```
If the API call fails (network issue, rate limit), the store silently falls back to these hardcoded approximate rates so the UI never breaks.

**The `convert()` function:**
```js
function convert(priceInRwf) {
    const converted = priceInRwf * rates.value[selected.value]
    if (selected.value === 'RWF') return Math.round(converted).toLocaleString()
    return converted.toFixed(2)
}
```
- RWF values are rounded to whole numbers (no cents in Rwandan Francs)
- USD and EUR values show 2 decimal places
- `toLocaleString()` adds thousands separators for large RWF values (e.g. `1,500,000`)

**The `symbol` computed property:**
```js
const symbols = { RWF: 'RWF', USD: '$', EUR: '€' }
const symbol = computed(() => symbols[selected.value])
```
This is used in table headers and price cells to always show the correct currency label.

---

#### Step 2 — `AdminInventoryView.vue`

`useCurrencyStore` was imported and `fetchRates()` called in `onMounted` so rates are fetched as soon as the page loads.

A currency button group was added to the header:
```html
<button v-for="c in ['RWF', 'USD', 'EUR']" :key="c"
    @click="currencyStore.setCurrency(c)"
    :style="currencyStore.selected === c ? 'background: var(--primary); color: #fff;' : '...'">
    {{ c }}
</button>
```
The active button is highlighted using an inline `:style` binding that checks `currencyStore.selected`.

Table headers and price cells were updated:
```html
<th>Price ({{ currencyStore.symbol }})</th>
<td>{{ currencyStore.symbol }} {{ currencyStore.convert(item.price) }}</td>
```

The modal price input label was updated to make it clear that regardless of what currency is displayed, the value entered is always saved in RWF:
```
Price (RWF — stored in Rwandan Francs)
```

---

#### Step 3 — `AdminDashboardView.vue`

The same currency store was wired in. The Total Stock Value stat card was updated:
```html
<div class="stat-number">{{ currencyStore.symbol }} {{ currencyStore.convert(totalValue) }}</div>
```

The `totalValue` computed property still calculates in RWF (sum of `price * quantity`), and `convert()` handles the display conversion — keeping the calculation logic clean and separate from the display logic.

The Excel export was updated so column headers reflect the selected currency at the time of export:
```js
[`Price (${currencyStore.selected})`]: currencyStore.convert(item.price),
[`Stock Value (${currencyStore.selected})`]: currencyStore.convert(item.price * item.quantity),
```
This means if the admin exports while viewing in USD, the Excel file will have `Price (USD)` and `Stock Value (USD)` columns with the converted values.

**Why sync works automatically:**
Pinia stores are singletons — there is only one instance of `useCurrencyStore` in the entire app. When `setCurrency('USD')` is called from the Dashboard, the `selected` ref inside the store updates. Both the Dashboard and Inventory views are reading from that same ref, so Vue's reactivity system re-renders both pages instantly without any additional event emitters or watchers.

---

## 4. Home Page Transparent Overlay

### Background
The home page uses a photo of the BNR building as its background. The design intent was to have a semi-transparent brown panel on the left showing the logo and title, while the building — including the "BNR" text physically on the building — remains visible through it.

### The Problem
The original layout used CSS Flexbox with two equal `flex: 1` children side by side:
- Left child: `.login-branding` — solid brown background, took up 50% of the screen
- Right child: `.login-bg` — the background image, took up the other 50%

This meant the building image was only visible on the right half of the screen, and the left panel was completely opaque — hiding the building entirely on that side.

Additionally, a `::after` pseudo-element added a dark gradient over the image:
```css
.login-bg::after {
    background: linear-gradient(to right, rgba(107,42,0,0.2), rgba(107,42,0,0.6));
}
```
This further obscured the building and the BNR text on it.

### The Fix

**`style.css` changes:**

`.login-bg` was changed from a flex sibling to a full-page absolute background:
```css
.login-bg {
    position: absolute;
    inset: 0;           /* covers top, right, bottom, left = full page */
    background-size: cover;
    background-position: center;
    z-index: 0;         /* sits behind everything */
}
```

The `::after` gradient overlay was removed entirely so the building image is fully visible.

`.login-branding` was changed to sit on top of the background:
```css
.login-branding {
    position: relative;
    z-index: 10;        /* sits above the background image */
    width: 45%;
}
```

**`HomeView.vue` changes:**

The background image div was moved to render before the branding panel in the HTML so the stacking order is correct. The branding panel's inline style was updated:
```html
style="background: rgba(107, 42, 0, 0.45); backdrop-filter: blur(2px);"
```

- `rgba(107, 42, 0, 0.45)` — the brown color at 45% opacity, letting the building show through
- `backdrop-filter: blur(2px)` — a subtle frosted glass effect that softens the building image behind the text, making the text easier to read without fully hiding the image

The result is the BNR building fills the entire page, the left panel is a translucent brown overlay, and the "BNR" text on the physical building is visible through it on the right side.

---

## Running the Project

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Port Conflict (if backend fails to start)
```bash
netstat -ano | findstr :8080
taskkill /F /PID <PID>
```
