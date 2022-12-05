# TODOs

- ~~add support for grafana alerts~~
- add support for wazooh alerts
- query elastic and alert
- server push (https://www.youtube.com/watch?v=5PQR9_Q0vaw)
- tags: filter by operators AND or OR
- redesigned journal (not table, but a list with show details)
- copy to clipboard button
- configure labels for CID (if needed also from external_labels)
- function time_of_max
- downsample - from 1 min (ie 4 metrics) create 1 metric
- generate report, configure data in report, configure query for report
- metric simulator
- tenants, different providers, data sources


https://www.primefaces.org/showcase/ui/data/timeline/basic.xhtml



https://www.primefaces.org/showcase


Search
Daj v datatable

<f:facet name="header">
							<p:outputPanel>
								<h:outputText value="Search all fields: " />
								<p:inputText id="globalFilter" onkeyup="PF('activeTable').filter()" style="width:150px" placeholder="Enter keyword"/>
							</p:outputPanel>
						</f:facet>
