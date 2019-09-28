# Testing Alertmonitor

Send webhook message from postman:

URL: http://{{HOSTNAME}}:8080/alertmonitor/webhook/

In header set user-agent to Alertmanager

Set body:

Alarm:

`
{"receiver":"alertmonitor_swarm","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.30","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.30"},"annotations":{"description":"172.29.254.30 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"},{"status":"firing","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.50","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.50"},"annotations":{"description":"172.29.254.50 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"},{"status":"firing","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.66","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.66"},"annotations":{"description":"172.29.254.66 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"}],"groupLabels":{"alertname":"ICMP ping failed"},"commonLabels":{"alertname":"ICMP ping failed","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed"},"commonAnnotations":{},"externalURL":"http://:9093","version":"4","groupKey":"{}/{severity=~\"^(?:^(critical|major|minor|warning|informational)$)$\"}:{alertname=\"ICMP ping failed\"}"}
`

Clear:

`
{"receiver":"alertmonitor_swarm","status":"resolved","alerts":[{"status":"resolved","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.30","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.30"},"annotations":{"description":"172.29.254.30 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"},{"status":"resolved","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.50","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.50"},"annotations":{"description":"172.29.254.50 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"},{"status":"resolved","labels":{"alertname":"ICMP ping failed","instance":"172.29.254.66","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed","summary":"No physical connection to 172.29.254.66"},"annotations":{"description":"172.29.254.66 does not reply to ICMP pings"},"startsAt":"2019-09-27T16:27:56.612666101Z","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://9ed35ff90bf8:9090/graph?g0.expr=probe_success%7Bjob%3D%22blackbox-icmp%22%7D+%3D%3D+0\u0026g0.tab=1"}],"groupLabels":{"alertname":"ICMP ping failed"},"commonLabels":{"alertname":"ICMP ping failed","job":"blackbox-icmp","monitor":"monis","severity":"major","tags":"icmp, ping failed"},"commonAnnotations":{},"externalURL":"http://:9093","version":"4","groupKey":"{}/{severity=~\"^(?:^(critical|major|minor|warning|informational)$)$\"}:{alertname=\"ICMP ping failed\"}"}
`


