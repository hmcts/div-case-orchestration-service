# Job Scheduler

The job scheduler module allows the service schedule jobs and cron jobs within the application. 
For example, scheduler can execute a specific task every Sunday at 2am.


## Setup configuration

Scheduler database support is integrated with spring-boot. Here is a config example
```
    spring:
      datasource:
        scheduler:
          name: scheduler
          driver-class-name: org.postgresql.Driver
          url: jdbc:postgresql://${CHEDULER_DB_HOST:localhost}:${SCHEDULER_DB_PORT:5432}/${SCHEDULER_DB_NAME:scheduler}${SCHEDULER_DB_CONN_OPTIONS:}
          username: ${SCHEDULER_DB_USER_NAME:scheduler_user}
          password: ${SCHEDULER_DB_PASSWORD:scheduler_password}
          properties:
          charSet: UTF-8
```

### Schedule properties

| Key  | Description| value |
| ------------- | ------------- | ------------- |
| scheduler.enabled  | Enables or disables the schedule| true / false, default true|
| scheduler.re-create  | Deletes old cron jobs| true / false, default true|


### How to create Cron schedule

Cron jobs can be schedule via properties with the following structure

``` 
schedules:
       - name: 'Test Schedulder'
         enabled: false
         cronGroup: "NIGHTLY_CRON"
         description: 'Scheduler to test'
         jobClass: 'uk.gov.hmcts.reform.divorce.orchestration.job.ScheduleTestJob'
         cron: ${SCHEDULER_SCHEDULES_TEST_CRON:0 0/1 * 1/1 * ? *}
 ``` 