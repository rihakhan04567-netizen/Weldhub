# WeldHub Firebase Firestore Database Schema

This document details the complete Firestore schema designed to support **WeldHub**—India's Elite Heavy Steel & Gate Marketplace. This structure is built for scalability, fast sub-millisecond querying, proper subcollection encapsulation, location-based querying (Geoqueries), and granular security.

---

## High-Level Schema Overview

WeldHub utilizes a hybrid structure of **flat collections** for primary entities, with **nested subcollections** for entity-scoped data (like portfolio reviews and structural sub-chats).

```
Firestore Root
│
├── 📁 users (Collection)
│   └── 📄 {userId} (Document)
│
├── 📁 welders (Collection)
│   └── 📄 {welderId} (Document)
│       ├── 📁 reviews (Subcollection)
│       └── 📁 portfolios (Subcollection)
│
├── 📁 designs (Collection)
│   └── 📄 {designId} (Document)
│       └── 📁 likes (Subcollection)
│
├── 📁 bookings (Collection)
│   └── 📄 {bookingId} (Document)
│       └── 📁 chats (Subcollection)
│
└── 📁 projects (Collection)
    └── 📄 {projectId} (Document)
```

---

## 1. `users` Collection
Stores core authentication and profile details for all platform participants (Customers, Welders, Admins, Contractors).

* **Path**: `/users/{userId}`
* **Document ID**: `{userId}` (Matches Firebase Auth UID)

```json
{
  "id": "USR_9f2a4b8c",
  "name": "Sanjay Kumar",
  "phone": "+919876543210",
  "whatsappNumber": "+919876543210",
  "role": "Customer", // Enums: "Customer", "Welder", "Fabricator", "Contractor", "Admin"
  "avatarUrl": "https://firebasestorage.googleapis.com/.../sanjay_avatar.jpg",
  "fcmToken": "f7yA2...B9oP", // For real-time push notifications
  "location": {
    "name": "Sector 18, Noida, UP",
    "geo": {
      "_latitude": 28.5708,
      "_longitude": 77.3259
    }
  },
  "createdAt": "2026-06-30T12:00:00Z", // timestamp
  "updatedAt": "2026-06-30T12:00:00Z"  // timestamp
}
```

---

## 2. `welders` Collection
A specialized collection containing profiles for welders, fabricators, and contractors. Uses the same ID as the matching `users` document for efficient `O(1)` joining.

* **Path**: `/welders/{welderId}`
* **Document ID**: `{welderId}` (Matches Firebase Auth UID)

```json
{
  "userId": "WLD_5e1b8c2d",
  "name": "Rajesh Sharma",
  "experienceYears": 12,
  "rating": 4.9,
  "ratingCount": 42,
  "isVerified": true,
  "availableToday": true,
  "emergencyService": true,
  "about": "Specialist in heavy structural arc welding, automatic sliding gates, and ornamental safety grills with 12+ years of industrial experience.",
  "skills": ["Arc Welding", "TIG Welding", "Gate Automation", "CAD Sheet Analysis"],
  "services": ["Gate Installation", "Staircase Railing", "Emergency Repair", "Rust Proofing"],
  "machines": ["IGBT Inverter Arc Welder", "CO2 MIG Welding Machine", "Heavy Duty Angle Cutter"],
  "certifications": ["AWSD1.1 Structural Steel", "Govt ITI Metal Fabrication Diploma"],
  "languagesSpoken": ["Hindi", "Punjabi", "English"],
  "workshopImages": [
    "https://firebasestorage.googleapis.com/.../shop1.jpg",
    "https://firebasestorage.googleapis.com/.../shop2.jpg"
  ],
  "location": {
    "name": "Noida Phase 2, Industrial Area",
    "geo": {
      "_latitude": 28.5355,
      "_longitude": 77.3910
    }
  },
  "stats": {
    "completedBookings": 154,
    "activeJobs": 3
  },
  "createdAt": "2026-05-10T08:30:00Z",
  "updatedAt": "2026-06-30T23:45:00Z"
}
```

### Subcollection: `reviews`
Stores customer ratings, feedback, and photos from completed bookings.

* **Path**: `/welders/{welderId}/reviews/{reviewId}`
* **Document ID**: Auto-generated

```json
{
  "customerId": "USR_9f2a4b8c",
  "customerName": "Sanjay Kumar",
  "customerAvatar": "https://firebasestorage.googleapis.com/.../sanjay_avatar.jpg",
  "rating": 5,
  "reviewText": "Excellent work! Fabricated and installed a 12-foot laser-cut main gate in record time. Completely flawless alignment.",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/.../finished_gate.jpg"
  ],
  "timestamp": "2026-06-25T15:10:00Z"
}
```

### Subcollection: `portfolios`
Before/After blueprints and workshop progress shots.

* **Path**: `/welders/{welderId}/portfolios/{portfolioId}`
* **Document ID**: Auto-generated

```json
{
  "title": "Modern Laser-Cut Sliding Main Gate",
  "beforeImgUrl": "https://firebasestorage.googleapis.com/.../before_site.jpg",
  "duringImgUrl": "https://firebasestorage.googleapis.com/.../workshop_frame.jpg",
  "finalImgUrl": "https://firebasestorage.googleapis.com/.../final_sliding_gate.jpg",
  "cost": 85000.0,
  "durationDays": 7,
  "materialUsed": "Mild Steel & SS-304 Accents",
  "location": "Greater Noida, UP",
  "customerReview": "Smooth rolling mechanism, precise fit.",
  "timestamp": "2026-06-01T10:00:00Z"
}
```

---

## 3. `designs` Collection
Stores ready-made gate blueprints, staircases, and safety grills for structural visualization and automated estimation.

* **Path**: `/designs/{designId}`
* **Document ID**: Auto-generated

```json
{
  "id": "DSN_6718a2b",
  "title": "Geometric Modern Laser-Cut Main Gate",
  "category": "Laser Cut Gate", // Enums: "Main Gate", "SS Gate", "Sliding Gate", "Laser Cut Gate", "Balcony", "Staircase", "Grill"
  "imageUrl": "https://firebasestorage.googleapis.com/.../gate_blueprint_3d.jpg",
  "estimatedCost": 72500.0,
  "materialUsed": "Mild Steel (MS) Hollow Sections",
  "pipeSize": "50mm x 50mm (Outer Frame)",
  "sheetThickness": "2.0 mm (Laser Plate)",
  "difficultyLevel": "Medium", // Enums: "Easy", "Medium", "Hard"
  "estimatedTimeDays": 5,
  "colorOptions": ["Industrial Slate Gray", "Matte Black", "Bronze Texture"],
  "similarDesigns": ["DSN_1234abc", "DSN_5678xyz"],
  "tags": ["laser-cut", "sliding", "heavy-duty", "modern-home"],
  "createdAt": "2026-06-01T00:00:00Z"
}
```

### Subcollection: `likes`
Provides an efficient subcollection mapping of which users liked/starred this design. This pattern avoids exceeding the 1MB document size limit of an array-in-document.

* **Path**: `/designs/{designId}/likes/{userId}`
* **Document ID**: `{userId}` (UID of the liking user)

```json
{
  "timestamp": "2026-06-30T23:50:00Z"
}
```

---

## 4. `bookings` Collection
Handles site bookings, scheduling, cost negotiations, and payments.

* **Path**: `/bookings/{bookingId}`
* **Document ID**: Auto-generated (or structured prefix e.g., `WH-2026-XXXXX`)

```json
{
  "id": "BK_2289a3c",
  "customerId": "USR_9f2a4b8c",
  "customerName": "Sanjay Kumar",
  "customerPhone": "+919876543210",
  "welderId": "WLD_5e1b8c2d",
  "welderName": "Rajesh Sharma",
  "designId": "DSN_6718a2b", // Optional reference if design-based booking
  "designTitle": "Geometric Modern Laser-Cut Main Gate",
  "date": "2026-07-05T09:00:00Z", // Scheduled fabrication/site-visit date
  "siteAddress": "Plot B-45, Sector 44, Noida, UP",
  "location": {
    "name": "Sector 44, Noida",
    "geo": {
      "_latitude": 28.5611,
      "_longitude": 77.3392
    }
  },
  "status": "In Progress", // Enums: "Pending", "Confirmed", "In Progress", "Completed", "Cancelled"
  "notes": "Ensure rust-preventative primer coat (Red Oxide) is thoroughly applied prior to final paint layer.",
  "advancePaid": 15000.0,
  "totalCost": 72500.0,
  "progressPercentage": 40,
  "invoiceUrl": "https://firebasestorage.googleapis.com/.../invoice_bk_2289a3c.pdf",
  "createdAt": "2026-06-29T10:15:00Z",
  "updatedAt": "2026-06-30T23:30:00Z"
}
```

### Subcollection: `chats`
Contains message strings, voice snippets, and digital invoices exchanged within a specific booking timeline.

* **Path**: `/bookings/{bookingId}/chats/{messageId}`
* **Document ID**: Auto-generated

```json
{
  "senderId": "WLD_5e1b8c2d",
  "receiverId": "USR_9f2a4b8c",
  "content": "Quotation submitted for 12ft x 7ft gate using premium MS Pipes. Please review.",
  "timestamp": "2026-06-30T23:55:00Z",
  "type": "quotation", // Enums: "text", "image", "quotation"
  "isRead": true,
  "quotation": {
    "lineItems": [
      { "item": "MS Box Pipes 50x50x2mm", "qty": 8, "rate": 1800.0, "amount": 14400.0 },
      { "item": "Laser Cut MS Plate 2mm", "qty": 2, "rate": 12500.0, "amount": 25000.0 },
      { "item": "Labor, Installation & Hinges", "qty": 1, "rate": 15000.0, "amount": 15000.0 }
    ],
    "totalAmount": 54400.0,
    "terms": "50% advance before fabrication, balance upon completion."
  }
}
```

---

## 5. `projects` Collection
Manages in-depth fabrication lifecycles, installation milestones, materials checklists, and Gantt charts for massive architectural gates and structural steelworks.

* **Path**: `/projects/{projectId}`
* **Document ID**: Auto-generated

```json
{
  "id": "PRJ_1029z8y",
  "bookingId": "BK_2289a3c",
  "title": "Fabrication of Heavy Laser Cut Main Gate",
  "customerId": "USR_9f2a4b8c",
  "welderId": "WLD_5e1b8c2d",
  "status": "Fabrication", // Enums: "Planning", "Fabrication", "Installation", "Handover"
  "startDate": "2026-06-30T00:00:00Z",
  "targetEndDate": "2026-07-08T00:00:00Z",
  "progress": 40,
  "milestones": [
    {
      "milestoneId": "m1",
      "title": "Design Finalization & Blueprints",
      "targetDate": "2026-06-30T00:00:00Z",
      "status": "Completed",
      "paymentTriggerAmount": 15000.0
    },
    {
      "milestoneId": "m2",
      "title": "Welding and Outer Frame Assembly",
      "targetDate": "2026-07-03T00:00:00Z",
      "status": "Pending",
      "paymentTriggerAmount": 20000.0
    },
    {
      "milestoneId": "m3",
      "title": "Laser Plate Mounting & Rust Priming",
      "targetDate": "2026-07-05T00:00:00Z",
      "status": "Pending",
      "paymentTriggerAmount": 0.0
    },
    {
      "milestoneId": "m4",
      "title": "Site Handover and Level Alignment",
      "targetDate": "2026-07-08T00:00:00Z",
      "status": "Pending",
      "paymentTriggerAmount": 19400.0
    }
  ],
  "materialsList": [
    { "material": "MS Box Pipes (50x50mm)", "quantity": "8 units", "status": "On Site" },
    { "material": "Custom Laser Plates", "quantity": "2 sheets", "status": "Procured" },
    { "material": "Red Oxide Rust Primer", "quantity": "5 Liters", "status": "Required" }
  ],
  "sitePhotos": [
    "https://firebasestorage.googleapis.com/.../progress_frame_1.jpg"
  ],
  "createdAt": "2026-06-29T11:00:00Z",
  "updatedAt": "2026-06-30T23:55:00Z"
}
```

---

## Firestore Compound Indexing

To support complex filtering and ordering, configure the following Firestore Composite Indexes:

1. **Querying Near-by Welders by verification & rating:**
   * Collection: `welders`
   * Fields: `isVerified` (Ascending), `rating` (Descending)
2. **Querying Designs by Category & budget limit:**
   * Collection: `designs`
   * Fields: `category` (Ascending), `estimatedCost` (Ascending)
3. **Querying Bookings by status sorted by schedule date:**
   * Collection: `bookings`
   * Fields: `status` (Ascending), `date` (Descending)

---

## Firebase Security Rules (Target Draft)

Ensure strict access controls so users can only view and mutate safe documents:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Core function helpers
    function isAuthenticated() {
      return request.auth != null;
    }
    function isOwner(userId) {
      return request.auth.uid == userId;
    }
    
    // User Rules
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(userId);
    }
    
    // Welder Rules
    match /welders/{welderId} {
      allow read: if isAuthenticated();
      allow write: if isOwner(welderId);
      
      match /reviews/{reviewId} {
        allow read: if isAuthenticated();
        allow create: if isAuthenticated() && request.resource.data.customerId == request.auth.uid;
        allow update, delete: if false; // Reviews are immutable for fairness
      }
      
      match /portfolios/{portfolioId} {
        allow read: if isAuthenticated();
        allow write: if isOwner(welderId);
      }
    }
    
    // Designs Rules
    match /designs/{designId} {
      allow read: if isAuthenticated();
      allow write: if isAuthenticated() && request.auth.token.role == "Admin";
      
      match /likes/{userId} {
        allow read, write: if isOwner(userId);
      }
    }
    
    // Bookings Rules
    match /bookings/{bookingId} {
      allow read, write: if isAuthenticated() && 
        (resource == null || request.auth.uid == resource.data.customerId || request.auth.uid == resource.data.welderId);
        
      match /chats/{messageId} {
        allow read, write: if isAuthenticated() && 
          (request.auth.uid == get(/databases/$(database)/documents/bookings/$(bookingId)).data.customerId ||
           request.auth.uid == get(/databases/$(database)/documents/bookings/$(bookingId)).data.welderId);
      }
    }
    
    // Projects Rules
    match /projects/{projectId} {
      allow read: if isAuthenticated() && 
        (request.auth.uid == resource.data.customerId || request.auth.uid == resource.data.welderId);
      allow write: if isAuthenticated() && request.auth.uid == resource.data.welderId;
    }
  }
}
```
