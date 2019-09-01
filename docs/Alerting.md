# Alerting

This is an article on alerting philosophy, how alerts should look, what data they should provide and how to correlate them with their clear events.


## Events, alarms and clears

In general event is any message that device or application sends to the world to notify the change of its state. Event could be anything:

1. link down or link up
2. CPU overloaded
3. device overheating
4. no connection
5. service not running
6. opened or closed doors
7. configuration has changed
8. unauthorized access
9. the sun is shining
10. it's raining
11. ...

Events may contain only informational messages that something happened and usually don't describe any state.

The application can send message when it detects an unauthorized access to some resource to notify the security about intruder. There is no need for opposite event indicating a successful authorized access. That's why such message cannot be treated like alarm - because it has no clear.

The application can also send message when it detects that configuration parameters were changed. It is also not expected to receive clear (would configuration-restored event make it clear? it makes no sense). If it matters weather the configuration was successfully changed or failed, then it would make it reasonable to call it alarm-clear pair.

It is still subject of debates weather the sun is clear and rain is alarm or vice versa. Let's just say they are both events describing current weather conditions. Actually, hail would make it alarm and any other weather its clear event.

> If the event will have its opposite event, then it must be alarm.

Other type of events describe the state alerting condition and are therefore called alarms (examples 1-5). They usually came in pairs. First event indicates that something stopped working (alarm) and second event notifies that something is now working normally (clear).

Routers send alarm LinkDown when they detect that there is no communication on Ethernet interface (eg. cable disconnected). When connection is restored a LinkUp message is sent (aka clear).

When application cannot connect to another resource (eg. database or another service or api) it can send alarm. When connection is successful it can send clear.

Some alarms are triggered when observed value exceeds a threshold limit. Example would be: CPU over 90% or Device is overheating (85 C).

> Alarms always come in pairs.

##### Are clears also alarms?

The format of alarm and clear message should always be the same. Weather the received event is alarm or clear should be determined by severity.

## Severity and priority

I saw many weird examples of severity usages, from more-urgent to less-critical, from asterisks to L1, L2, L3.. whatever that means. Is it critical or not? How it can be less-critical?

Let's just stick to the following severities (also compliant to X733 standard):
- critical
- major
- minor
- warning
- clear
- informational
- indeterminate

First 4 describe how bad the problem is.

Clear has special meaning - it should remove the alarm raised by one of the first 4 severities.

Informational (not defined in X733) is meant only to be notification about some one-time event. Informational event has no severity.
In some cases it would be reasonable to have informational messages categorized by severity, but then you need to distinguish alarms from events by other means (eg. additional labels).

Indeterminate - something undefined or unknown. I have no idea when would that be.

> Imagine you see two critical alarms: LinkDown and FireInTheHouse. To which one would you react immediately? In a bunch of critical alarms you can easily overlook really important alarm.

This is why we need to add priority level to alarms. Priority tells how fast we must react to alarm.

Possible values (but not limited to):
- high
- medium
- low

**High** means that you need to respond immediately (eg. fire).

**Low** means there is no need to jump out of bed and scream for help (eg. cpu overloaded). It can wait. But still that does not make alarm any less critical than it is. It should be fixed anyway.

Medium is somewhere in between.

> If you combine severity and priority you can get 15 combinations of true severity levels (clear and indeterminete don't need priority).

## Alarm names

Every alarm has its own name which should be short and briefly provide high-level information about alarm. Examples:
- Node Down
- Fan failed
- Disk full
- No connection to MySQL

## Source info

Alarms must provide information where exactly the fault happened.

First of all which device sent the alarm - IP address or hostname or its symbolic name.

Second the information where in the device is problem. This strongly depends on type of alarming object, but here are a few examples:
- Link Down on port 1/1/2 on 11.22.33.44

> Uniqueness of alarm is determined by alarm name, device name or id and alarm source info.

> Values that are constantly changing should never be included in source info (eg. current temperature value or CPU percentage). Put them somewhere else.

## Additional info

This is a place to put all other information about alarm that does not fit anywhere else, including changeable values, like current values, threshold values...

I prefer adding custom tags to alarms for sorting and filtering.

## ProbableCause and EventType

Yes, X733 standard also recommends categorizing alarms according to ProbableCause and EventType. These are just another two labels to attach them to alarms for sorting and filtering.

For example: Link Down alarm (which affects communications) would have eventType set to Communications and probableCause set to Loss of signal.

Search for their the values on internet.

## Correlation

Correlation refers to matching alarm-clear pairs. Uniqueness of alarm is determined by alarm name, device name or id and alarm source info. The same data should be present in both - alarm and clear messages. The only difference would be severity which tells weather alarm should be raised or cleared.

There are some cases where event with another name as original alarm actually means clear. Such example would be (SNMP): LinkDown and LinkUp. They are two different traps, but they contain the same set of data (ifIndex, ifAdminStatus, ifOperStatus).

## Synchronization

Good monitoring system always shows actual current alarm state. What if all connections go down and no new events can come to monitoring system? The answer is simple - you'll get nothing.

But when the connections restore you cannot be 100% sure that current alarm state is still up-to-date. What if we missed some critical events?

To update current alarm state, monitoring system should periodically synchronize alarms with devices. This is not always a straightforward process. First question is weather the device supports re-sending of all currently selected alerts? Then how can I get them?



