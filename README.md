# BPApp
Android app code for Blood Pressure Monitor - SP2022 Capstone Project in collaboration with AdventHealth

## Notes - 4/13/2022
This is an incomplete project, and we haven't gone through to clean up our code extensively yet. The only things we still need to finish are:
1. Bluetooth functionality
2. Data management settings (mainly just deleting entries in the database so it doesn't get too big)
3. Any last-minute touches, such as colors and fonts.
These should be finished by next week -- worst case scenario is that it is done before the 27th.

Code of interest:
1. app/src/main/java/com/example/bloodpressuremonitor/ -- contains MainActivity.java (where, among other things, we have the code for sending a CSV file containing the blood pressure data through Gmail), SettingsActivity.java, and DBHandler.java (which sets up the SQLite database).
2. app/src/main/java/com/example/bloodpressuremonitor/ui -- each subfolder here is for a different section of the app. The home screen doesn't have anything on it right now. In display_data and read_data, the DisplayDataFragment.java and ReadDataFragment.java files have the code used to display and read data in the app.
3. app/src/main/res/layout -- each XML file has the layout code for each screen in the app. fragment_gallery and fragment_slideshow refer to the layouts for the Read Data screen and the Display Data screen, respectively.

Admittedly this is our first time developing a mobile application, so this is not a perfect application. We also dropped work on an iOS version of the app due to difficulties with developing for Apple. However, so far, we have successfully implemented a basic database into the application, along with functionalities for sending and displaying data to the user. I have included two .pdf files; these are essentially progress checks that we had to do as assignments. If you have any questions, email me (abeeching7994@floridapoly.edu) and I will forward them to the rest of the Computer Science-based team.

## Progress Reports
[Prototype #1 Report (January - February 2022).pdf](https://github.com/abeeching/BPApp/files/8484682/Prototype.1.Report.January.-.February.2022.pdf)

[Prototype #2 Report (February - March 2022).pdf](https://github.com/abeeching/BPApp/files/8484684/Prototype.2.Report.February.-.March.2022.pdf)
