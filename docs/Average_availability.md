# Average availability

[october 2022]

Use case: What is the average availability of all instances in the last hour (in %)?

Availability of an instance is how much time the instance was reachable within observed interval.
We will focus on the `up` metric, which can have only values 0 or 1.

Mathematically, average availability is ratio between number of samples where `up=1` and total number of `up` samples.
To calculate average value of the `up` metric to get average availability of each instance (in last 1 hour interval):

```
avg_over_time(up[1h]) * 100
```

To get average availability of all instances, we can just simply average all averages over time:

```
avg(avg_over_time(up[1h]) * 100
```

Things get a bit trickier when we introduce 'probe' exporters such as blackbox-exporter or snmp-exporter. 
In this case, Prometheus indirectly scrapes metrics from end targets. Prometheus actually collects metrics from 
the exporter, which always results in `up` metric being 1, regardless of the real status of the target instance.
'Probe' exporter in these cases returns the status of instances in metric `probe_success`.

So, by using 'probe' exporter Prometheus adds two metrics for each target instance:
- `up` - status of the exporter itself (because it was successfully scraped it always has value 1)
- `probe_success` - the real status of target instance (many think this should be reflected in `up` metric).


Let’s observe 2 ordinary instances and 2 instances monitored by 'probe' exporter and represent the last 4 metrics in a table.
{N} represents 4 different instances.


| Metric            | Metric values      | Avg_over_time |
|-------------------|--------------------|---------------|
| up{1}             | ... 1  1  1  1     | 1             |
| up{2}             | ... 1  1  0  0     | 0,5           |
| up{3}             | ... 1  1  1  1     | 1             |
| up{4}             | ... 1  1  1  1     | 1             |
| probe_success{3}  | ... 1  1  1  1     | 1             |
| probe_success{4}  | ... 0  0  1  1     | 0,5           |

Averaging the last column will give 83% availability. Nice result, but it is wrong.  We shouldn't take metrics
`up{3}` and `up{4}` into account.

We need to consider the following rules:
- `up` metric is irrelevant for instances monitored by 'probe'exporter (it always equals 1 and averages to 1 anyway)
- there is an equal number of `up` metrics (with value 1 of probe exporter) and `probe_success` metrics (regardless of its value 1 or 0).

In this calculation we will not use the `avg` function of Prometheus, but we will calculate average value on our own: 
sum all averages over time and divide by number of samples (see third column in table above):

```
(sum(avg_over_time(up[1h])) + sum(avg_over_time(probe_success[1h]))) / count(up) * 100
```

The problem with equation above is that it still includes `up` metric of the 'probe'exporter. Let's eliminate that in nominator and denominator.
Correct equation would be:

```
(sum(avg_over_time(up[1h])) + sum(avg_over_time(probe_success[1h])) - count(probe_success)) / (count(up) - count(probe_success)) * 100
```

where `count(probe_success)` equals to number of `up` metrics of the 'probe' exporter.

The equation is still valid if 'probe' exporter is down. In this case, there will be no `probe_success` metrics at all and the equation above will become much simpler:

```
sum(avg_over_time(up[1h])) / count(up) * 100
```

which in fact gives us the initial equation:

```
avg(avg_over_time(up[1h])) * 100
```


# Liveness

Use case: How many instances are live (reachable) at a particular moment (in %)?

Let's avoid averaging and limiting the result to 1h interval. We'll get another view on the availability of instances.

```
(count(up == 1) + count(probe_success == 1) - count(probe_success)) / (count(up) - count(probe_success)) * 100
```

Here I just summed all successful `up` metrics and all successful `probe_success` metrics and subtracted the number of 
all `up` metrics from 'probe' exporter (ie. `count(probe_success)`) and divide by number of all `up` metrics without `probe_success` metrics.


# Conclusion

I am not sure about the term *Liveness*, probably *Reachability* would be more appropriate and grammatically correct.  

One of the benefits in this approach is that I avoided involving `job` label in equations.

If you observe both results (Average availaility and Liveness) you'll notice that in stable 
environment, both values tend to be equal. In more turbulent environment, the values will differ, mainly 
because of averaging the availability over 1h interval.
