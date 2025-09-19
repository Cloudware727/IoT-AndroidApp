# Brewsta

**Mobile app + IoT coffee/tea dispenser**

Brewsta is a mobile-first Android app (Java + Fragments) that pairs with a Raspberry Pi-based dispenser to let users craft, save and replay custom beverage recipes remotely. It was built as a student project to demonstrate full-stack IoT integration: a native Android client, simple REST API, and device controller code running on a Raspberry Pi.

---

## Table of Contents

* [Features](#features)
* [Screenshots](#screenshots)
* [Tech stack](#tech-stack)
* [Repository structure](#repository-structure)
* [Getting started (Android)](#getting-started-android)
* [Backend & Device (high level)](#backend--device-high-level)
* [API contract (example)](#api-contract-example)

---

## Features

* User registration and login (simple auth flow)
* Browse available drinks and scrollable carousel UI
* Choose shot intensity, sugar level and infusion temperature with intuitive controls
* Start/pause order and view live progress + temperature
* Order history and favorites
* Simple admin screen for assigning dispenser images and default drink selection
* Lightweight, recruiter-friendly codebase using standard Android architecture patterns

---

## Screenshots

Place the images in a `screenshots/` folder at the repo root and reference them from the README.

|                  Sign up | Login                   |
| -----------------------: | :---------------------- |
| `screenshots/signup.png` | `screenshots/login.png` |

\| Home / Selection | Order Summary |
\| `screenshots/home.png` | `screenshots/order_summary.png` |

\| Preparing overlay | History (no access) |
\| `screenshots/ready_overlay.png` | `screenshots/history_restricted.png` |

\| Recent orders | Settings (upload image) |
\| `screenshots/recent_orders.png` | `screenshots/settings.png` |

\| Favourites |
\| `screenshots/favourites.png` |

*(I named each file predictably — rename or move the actual images into the screenshots folder so the links render.)*

---

## Tech stack

* Android (Java, AndroidX, Fragments)
* Volley for HTTP requests (or Retrofit depending on the branch)
* Local persistence: SQLite / Room (check `app/src` for exact usage)
* Raspberry Pi device code: Python (controller that listens to REST commands / MQTT) — separate repo
* Optional: small Node/Flask API to bridge mobile <-> device

---

## Repository structure (example)

```
interface/                  # this Android app repo
├─ app/
│  ├─ src/main/java/...
│  ├─ src/main/res/...
│  └─ AndroidManifest.xml
├─ screenshots/             # add screenshots here for README
├─ build.gradle
└─ README.md

raspberrypi/                # separate repo for device code (do not mix)
├─ controller.py
├─ requirements.txt
└─ README.md
```

**Important:** keep the mobile app and the Raspberry Pi device code in separate repositories. That keeps CI, issues and deployment sane.

---

## Getting started (Android)

1. Install Android Studio (Arctic Fox or newer) and Java 11+.
2. Clone the repo:

```bash
git clone git@github.com:Cloudware727/Brewsta-Interface-App.git
cd Brewsta-Interface-App
```

3. Open the project in Android Studio. Let Gradle sync.
4. Connect an Android device (USB, enable USB debugging) or start an emulator.
5. Run the app (`Run > Run 'app'`) or use `./gradlew installDebug`.

### Environment / config

* Check `app/src/main/assets` or `res/values/strings.xml` for API base URL. Replace with your backend base URL (e.g. `http://192.168.0.10:5000`).
* If the app uses API keys or secrets, add them via Gradle properties or environment variables — do **not** commit secrets.

---

## Backend & Device (high level)

We recommend a small REST API running on a local server (Flask, Node, or similar) that exposes endpoints the Android app calls. The server then forwards commands (HTTP/MQTT) to the Raspberry Pi controller which performs heater/pump/valve actions and reports temperature/progress back.

Keep the device code in a separate repo called `Brewsta-Device` or `Brewsta-RPi`.

---

## API contract (example)

Simple examples so you know what to implement on the backend:

* `POST /auth/register`  — body `{ username, email, password }`
* `POST /auth/login`     — body `{ username, password }` => returns `token`
* `GET /drinks`          — returns available drinks and images
* `POST /orders`         — body `{ drink_id, shot, sugar, temperature }` => starts an order
* `GET /orders/:id`      — returns status `{ progress, temperature, status }`
* `GET /orders`          — returns order history

