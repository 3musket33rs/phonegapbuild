phonegapbuild
=============

PhoneGapBuild is part of the 3muket33rs mobile suite. You can package on the cloud your app built with [HTML5-mobile-scaffolding](https://github.com/3musket33rs/html5-mobile-scaffolding).
 We offer Grails plugin around mobile. You want to:


- to scaffold CRUD for a mobile first application? Header, footer, list with responsive UI powered by jQuery mobile... 
You can do it in minutes with html-generate-all command. No more GSP, you will work in single page mode with HTML5 and JavaScript.
- to geolocate your position. Easy use convention, add **latitude** and **longitude** attribute to you doamain class.
- geolocate with mongoDB we integrate with
- use Google Maps to add markers, move markers, we wrapped Google Map services for you
- offline mode and synchronization
- push notification using [event-push plugin](http://grails.org/plugin/events-push). We use GETfull API, minimizing server side calls. No need to fetch data from server. Get notified.
- want to package in hybrid. Easy use [3musket33rs phone gap build plugin](https://github.com/3musket33rs/phonegapbuild)

You want to know more... [Find full documentation here](http://3musket33rs.github.com/html5-mobile-scaffolding/)

Prerequisites
=============
Create an account on [PhoneGap Build](https://build.phonegap.com/)

Install it
===========

Add a dependency to BuildConfig.groovy:

    plugins {
        compile ":phonegapbuild"
        ...
    }

Add your credential in Config.groovy
phonegapbuild.username="..."
phonegapbuild.password="..."
phonegapbuild.phonegapversion="2.3.0"

To test it
===========

Go to

  localhost:8080\yourapp\app\initBuild

clik on push to cloud fourndry and wait for packaging

Give it a trial and send us feedback!
====================================

3mukete33rs on twitter @3muket33rs 
- Athos is Corinne Krych (@corinnekrych)
- Aramis is Sebastien Blanc (@sebi2706)
- Porthos is Fabrice Matrat (@fabricematrat)
- D'artagnain is Mathieu Bruyen (@mathbruyen)
