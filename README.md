### Naturalist Hiker's assistent / Capstone Project ###

### Project Description ###
This app is naturalist hikers' assistant. It helps hikers prepare for the hike, assists hikers during hiking and helps hikers reviewing their observation. However, the capstone project focuses on pre-trip preparation. 


|  |  |  |  |  |
|:-----|:-----|:-----|:-----|:-----|
| ![](screenshoots/setup_my_location.png) | ![](screenshoots/current_trip.png) | ![](screenshoots/trip_detail.png) | ![](screenshoots/plant_list.png) | ![](screenshoots/meetat_user_defined.png) |
| Setup my location | The home page<br> shows the upcoming trip | The trip details | The plants I am interested<br>to see at this trip | The meet up places |
| ![](screenshoots/plant_detail_fav.png) | ![](screenshoots/plant_list_on_hike.png) | ![](screenshoots/future_trip.png) |![](screenshoots/weather.png) | |
| A detail description<br> about a plant | During the hike, I <br>can mark the plant <br>when I saw it and take <br>photos if I like | The list of future trips | The weather report of<br>the hiking day | |

See details of capstone project in the document.   
  https://github.com/mingrutar/Capstone-Project/blob/master/documents/Capstone_Stage1.pdf

### Run the app ###
1) obtain an API key from Google Maps
2) create a resource file name 'google_maps_api.xml' under app/src/debug/res/values
3) replace the string "YOUR GOOGLE MAPS KEY" with your key.
  <resources>
    <string name="google_maps_key" translatable="false" templateMergeStrategy="preserve">
    YOUR GOOGLE MAPS KEY
    </string>
</resources>
### The screenshots and explanation ###
Setup after installation, one time only. The user defines the days prior the trip for a trip reminder, the preparation time before leaving the house on the hiking day.

![setup](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/setup_my_location.png?raw=true)

The screen of the upcoming trip

![current](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/current_trip.png?raw=true)

The screen of future trip

![future](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/future_trip.png?raw=true)

Trip details

![tripdetail](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/trip_detail.png?raw=true)

Plant list, the heart icon indicates user's favorite.

![plant_list](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/plant_list.png?raw=true)

Plant details, click the heart icon for marking favorite.

![plant_detail](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/plant_detail_not_fav.png?raw=true)

Meeting place: the red marker indicates user's home; the blue marker indicates the trip meeting place; the magenta marker indicates the user defined meeting place if he/she likes to meet else where.  

![meetup](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/meetat_user_defined.png?raw=true)

Checklist. A club has a checklist. The trip leader can enter a checklist for a trip. And a user can define his/her checklist. A checklist item can be optional or required. The user can ask app to remember his/her choice.

![Checklist](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/checklist.png?raw=true)

The app sends a notification prior the trip within the days defined by the user. The user can customize the remind day for a particular trip.  

![reminder](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/reminder_change_day.png?raw=true)

The weather information is available 6 days prior the trip

![weather](https://github.com/mingrutar/Capstone-Project/blob/master/screenshoots/weather.png?raw=true)
