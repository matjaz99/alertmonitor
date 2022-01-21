# TODOs

- ~~add support for grafana alerts~~
- add support for wazooh alerts
- server push (https://www.youtube.com/watch?v=5PQR9_Q0vaw)
- tags: filter by operators AND or OR
- redesigned journal (not table, but a list with show details)
- copy to clipboard button
- show targets by job
- configure labels for CID (if needed also from external_labels)
- store alarms in MongoDB



https://www.primefaces.org/showcase/ui/data/timeline/basic.xhtml


https://prom.devops.iskratel.cloud/prometheus/api/v1/query?query=ALERTS

https://prom.devops.iskratel.cloud/prometheus/api/v1/alerts


https://www.primefaces.org/showcase/ui/data/datatable/columns.xhtml



<h:panelGrid columns="3" style="margin:10px 0">
						<p:outputLabel for="template" value="Template: " style="font-weight:bold"/>
						<p:inputText id="template" value="#{dtColumnsView.columnTemplate}" size="50"/>
						<p:commandButton update="messagesTable" action="#{dtColumnsView.updateColumns}" value="Update" process="@parent" icon="pi pi-refresh" oncomplete="PF('notifsTable').clearFilters()"/>
					</h:panelGrid>


Search
Daj v datatable

<f:facet name="header">
							<p:outputPanel>
								<h:outputText value="Search all fields: " />
								<p:inputText id="globalFilter" onkeyup="PF('activeTable').filter()" style="width:150px" placeholder="Enter keyword"/>
							</p:outputPanel>
						</f:facet>
