# --- First database schema

# --- !Ups

create table sex (
  id                        bigint not null,
  name                      varchar(255),
  constraint pk_sex primary key (id))
;

create table person (
  id                        bigint not null,
  name                      varchar(255),
  age		                integer,
  email		              varchar(255),
  sex_id                bigint,
  constraint pk_person primary key (id))
;

create sequence sex_seq start with 1000;

create sequence person_seq start with 1000;

alter table person add constraint fk_person_sex_1 foreign key (sex_id) references sex (id) on delete restrict on update restrict;
create index ix_person_sex_1 on person (sex_id);


# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists sex;

drop table if exists person;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists sex_seq;

drop sequence if exists person_seq;

