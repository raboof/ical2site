# ical2site

Static site generator for event calendar sites.

Fetch one or more ICalendar calendars and turn them into an event calendar
site.

## Caching

For fast iteration, ICalendars are not fetched every time but stored in the
'cache' problem. There currently is no automatic cache invalidation (it's a
hard problem :) ), so just remove it when you need fresh data. 

## Facebook tokens

If your ICalendar URL needs a Facebook token as parameter (for example when
talking to https://github.com/raboof/facebook2ical),
add `FB_TOKEN` to the URL and add a github app client id and client secret
to the .env

## Contributions

Want to use this project to generate an event calendar for your community?
Feel free to get in touch, I'd love to collaborate! PR's and issues are
definitely welcome.
