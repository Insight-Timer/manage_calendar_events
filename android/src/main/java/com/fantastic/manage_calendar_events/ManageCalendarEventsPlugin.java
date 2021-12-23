package com.fantastic.manage_calendar_events;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.fantastic.manage_calendar_events.models.Calendar;
import com.fantastic.manage_calendar_events.models.CalendarEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ManageCalendarEventsPlugin
 */
public class ManageCalendarEventsPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler {

    private CalendarOperations operations;
    private final Gson gson = new Gson();
    private FlutterPluginBinding binding;
    private MethodChannel channel;


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if(operations == null && !call.method.equals("getPlatformVersion")) {
            result.error("99999", "CalendarOperation is not initialized", null);
            return;
        }

        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("hasPermissions")) {
            result.success(operations.hasPermissions());
        } else if (call.method.equals("requestPermissions")) {
            operations.requestPermissions();
        } else if (call.method.equals("getCalendars")) {
            ArrayList<Calendar> calendarArrayList = operations.getCalendars();
            result.success(gson.toJson(calendarArrayList));
        } else if (call.method.equals("getEvents")) {
            String calendarId = call.argument("calendarId");
            result.success(gson.toJson(operations.getAllEvents(calendarId)));
        } else if (call.method.equals("getEventsByDateRange")) {
            String calendarId = call.argument("calendarId");
            long startDate = call.argument("startDate");
            long endDate = call.argument("endDate");
            result.success(gson.toJson(operations.getEventsByDateRange(calendarId, startDate,
                    endDate)));
        } else if (call.method.equals("createEvent") || call.method.equals("updateEvent")) {
            String calendarId = call.argument("calendarId");
            String eventId = call.argument("eventId");
            String title = call.argument("title");
            String description = call.argument("description");
            long startDate = call.argument("startDate");
            long endDate = call.argument("endDate");
            String location = call.argument("location");
            String url = call.argument("url");
            boolean isAllDay = call.argument("isAllDay");
            boolean hasAlarm = call.argument("hasAlarm");
            CalendarEvent event = new CalendarEvent(eventId, title, description, startDate,
                    endDate, location, url, isAllDay, hasAlarm);
            operations.createUpdateEvent(calendarId, event);
            if (call.hasArgument("attendees")) {
                addAttendees(event.getEventId(), call);
            }
            result.success(event.getEventId());
        } else if (call.method.equals("deleteEvent")) {
            String calendarId = call.argument("calendarId");
            String eventId = call.argument("eventId");
            result.success(operations.deleteEvent(calendarId, eventId));
        } else if (call.method.equals("addReminder")) {
            String calendarId = call.argument("calendarId");
            String eventId = call.argument("eventId");
            long minutes = Long.parseLong(call.<String>argument("minutes"));
            operations.addReminder(calendarId, eventId, minutes);
        } else if (call.method.equals("updateReminder")) {
            String calendarId = call.argument("calendarId");
            String eventId = call.argument("eventId");
            long minutes = Long.parseLong(call.<String>argument("minutes"));
            result.success(operations.updateReminder(calendarId, eventId, minutes));
        } else if (call.method.equals("deleteReminder")) {
            String eventId = call.argument("eventId");
            result.success(operations.deleteReminder(eventId));
        } else if (call.method.equals("getAttendees")) {
            String eventId = call.argument("eventId");
            result.success(gson.toJson(operations.getAttendees(eventId)));
        } else if (call.method.equals("addAttendees")) {
            String eventId = call.argument("eventId");
            addAttendees(eventId, call);
        } else if (call.method.equals("deleteAttendee")) {
            String eventId = call.argument("eventId");
            Map<String, Object> attendeeMap = call.argument("attendee");
            String name = (String) attendeeMap.get("name");
            String emailAddress = (String) attendeeMap.get("emailAddress");
            boolean isOrganiser = attendeeMap.get("isOrganiser") != null ?
                    (boolean) attendeeMap.get("isOrganiser") : false;
            CalendarEvent.Attendee attendee = new CalendarEvent.Attendee(name, emailAddress,
                    isOrganiser);
            result.success(operations.deleteAttendee(eventId, attendee));
        } else {
            result.notImplemented();
        }
    }

    private void addAttendees(String eventId, MethodCall call) {
        List<CalendarEvent.Attendee> attendees = new ArrayList<>();
        List<Map<String, Object>> jsonList = call.argument("attendees");
        for (Map<String, Object> map : jsonList) {
            String name = (String) map.get("name");
            String emailAddress = (String) map.get("emailAddress");
            boolean isOrganiser = (boolean) map.get("isOrganiser");
            attendees.add(new CalendarEvent.Attendee(name, emailAddress, isOrganiser));
        }
        operations.addAttendees(eventId, attendees);
    }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        this.binding = binding;
        this.channel = new MethodChannel(binding.getBinaryMessenger(),
                "manage_calendar_events");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        this.channel.setMethodCallHandler(null);
        this.channel = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        Activity activity = binding.getActivity();
        this.operations = new CalendarOperations(activity, this.binding.getApplicationContext());
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.operations = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
       Activity activity = binding.getActivity();
       this.operations = new CalendarOperations(activity, this.binding.getApplicationContext());
    }

    @Override
    public void onDetachedFromActivity() {
        this.operations = null;
    }
}
