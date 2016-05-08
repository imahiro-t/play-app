# Notification

# --- !Ups

CREATE TABLE "notification" (
    "id" bigint(20) NOT NULL AUTO_INCREMENT,
    "subject" varchar(255),
    "action_date" date,
    "action_time" time,
    "notify_before" integer,
    "summary" varchar(255),
    "notification_date" timestamp,
    "sent" boolean,
    PRIMARY KEY ("id")
);

# --- !Downs

DROP TABLE "notification";
