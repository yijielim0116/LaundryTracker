# Laundry Tracker

Laundry Tracker is a Kotlin Android app for keeping track of the laundry machines in a building, so you can check which washers and dryers are free before heading down. It was built for **Mobile Application Development (Assignment 1)** at SETU.

## Features

- **Home screen** with a View Machines button
- **Add, list, edit and delete machines** (name, location, washer/dryer, status)
- **Search** machines by name or location, and **filter** by status with a drop-down
- **Cycle length** picked with a NumberPicker (10–120 minutes), shown only when a machine is *In use*
- **Toolbar menu** with Add, Clear filters and About
- **Up / Back navigation** between Home → Machines → Add/Edit
- **Data validation**: a machine must have a name
- **Saved to JSON**: machines are kept in `machines.json` and are still there after the app is closed
- **Unit tests** for both stores

## Screens

| Screen | Class | Layout |
|---|---|---|
| Home | `MainActivity` | `activity_main.xml` |
| Machines list | `MachineListActivity` + `MachineAdapter` | `activity_machine_list.xml`, `item_machine.xml` |
| Add / Edit machine | `AddEditActivity` | `activity_add_edit.xml` |

## Class diagram

![Laundry Tracker class diagram](docs/laundrytracker-uml.png)

([SVG version](docs/laundrytracker-uml.svg))

- The three screens extend `AppCompatActivity`. Home opens the list, and the list opens the form (sending the machine's id when editing).
- The machines list uses a `RecyclerView` with `MachineAdapter`, which turns each `MachineModel` into a row from `item_machine.xml`.
- Every screen uses the same store through `AppData.machines`, typed as the `MachineStore` interface.
- `MainApp` (an `Application` class) runs once when the app starts and sets `AppData.machines` to a `MachineJSONStore`.
- `MachineJSONStore` and `MachineMemStore` both implement `MachineStore`. The JSON store saves to a file with Gson; the memory store is used in the unit tests.

## Project structure

```
app/src/main/java/org/setu/laundrytracker/
├── activities/   MainActivity, MachineListActivity, AddEditActivity
├── adapters/     MachineAdapter
├── main/         MainApp, AppData
└── models/       MachineModel (+ MachineType, MachineStatus), MachineStore,
                  MachineMemStore, MachineJSONStore
```

## Persistence

Machines are saved by `MachineJSONStore` to `machines.json` in the app's private storage (`/data/data/org.setu.laundrytracker/files/`). The whole list is written after every add, edit or delete and read back when the app starts. New ids carry on from the highest saved id, so an id is never reused.

To see the file, open **View → Tool Windows → Device Explorer** in Android Studio and go to `data/data/org.setu.laundrytracker/files/machines.json`.

## Running the app

1. Open the project folder in Android Studio.
2. Let Gradle sync, choose an emulator (e.g. Pixel 4a, API 34) and press **Run**.

From the terminal:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

## Tests

Unit tests are in `app/src/test/java/org/setu/laundrytracker/models/`:

- `MachineMemStoreTest`: create, update, delete and cycle length on the in-memory store
- `MachineJSONStoreTest`: saves to a temporary file, then reopens it to check machines, edits, deletes and ids are kept

## Built with

Kotlin, AndroidX AppCompat, RecyclerView, Gson, JUnit.

## Development

Each feature has a GitHub issue, its own branch and small commits that reference the issue, merged into `main` through a pull request.
