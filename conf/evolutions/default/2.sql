# --- Sample dataset

# --- !Ups

insert into sex (id,name) values (  1,'Male');
insert into sex (id,name) values (  2,'Female');
insert into sex (id,name) values (  3,'Transgender');


insert into person (id,name,age,email,sex_id) values (  1,'zjc',22,'zjc@pku.edu.cn',1);
insert into person (id,name,age,email,sex_id) values (  2,'zyf',18,'zyf@pku.edu.cn',2);
insert into person (id,name,age,email,sex_id) values (  3,'jgz',20,'jgz@pku.edu.cn',1);
insert into person (id,name,age,email,sex_id) values (  4,'wz',20,'wz@pku.edu.cn',1);
insert into person (id,name,age,email,sex_id) values (  5,'liao',23,'liao@pku.edu.cn',1);
# --- !Downs

delete from person;
delete from sex;
