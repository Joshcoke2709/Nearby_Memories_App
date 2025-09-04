Scenario:
You are tasked with creating Nearby Memories, an Android application that allows users to capture and store memories associated with their current location.

Requirements:

Main Application Flow (Single Activity with Multiple Views)

The app should display one screen at a time and allow switching between different views such as:

Map / Current Location View: Shows the user’s current location and nearby memories.

Memory List View: Shows a scrollable list of all saved memories.

Memory Detail View: Shows detailed info of a selected memory.

The design should support dynamic screen changes within the same activity, rather than launching new activities for each screen.

Location Usage

Retrieve the device’s current location when adding a new memory.

Associate each memory with latitude and longitude.

Content Provider Usage

Use an existing Android content provider to enrich the memory. Examples could include:

Selecting a contact to associate with the memory (Contacts provider)

Choosing a photo from the gallery (MediaStore provider)

The selected content should be stored as part of the memory entry.

Network Interaction

When saving a memory, fetch additional context from a network source based on the location (e.g., weather, nearby landmark info, or a location description).

Display this information in the memory detail view.

Database Persistence

Store all memories locally so they persist across app restarts.

Each memory should include:

Title or description

Location coordinates

Optional associated content (contact or media URI)

Network-fetched data (e.g., weather info)

Detail View

Tapping a memory in the list should display its details on a separate view within the same activity.

Deliverables:

A fully functional Android project that demonstrates:

Dynamic single-activity navigation

Device location retrieval

Use of an existing content provider

Local database persistence

Network-based data fetching

