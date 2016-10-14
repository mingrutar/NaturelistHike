PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS plant_list (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   family VARCHAR NOT NULL,
   scientific VARCHAR NOT NULL,
   common VARCHAR NOT NULL,
   image_url VARCHAR NOT NULL,
   is_favor INTEGER DEFAULT 0,
  CONSTRAINT name_pl_unique UNIQUE (scientific ) ON CONFLICT REPLACE
 );
 CREATE TABLE IF NOT EXISTS plant_alias (
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    plant_id REFERENCES plant_list (_id),
    name VARCHAR NOT NULL
  );
CREATE VIEW IF NOT EXISTS favorite_plant AS
 SELECT *
 FROM plant_list
 WHERE is_favor = 1;

CREATE TABLE IF NOT EXISTS  trip (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   name VARCHAR NOT NULL,
   subtitle VARCHAR,
   th_lantitude REAL NOT NULL,
   th_longitude REAL NOT NULL,
   trip_url VARCHAR NOT NULL UNIQUE,
   distance REAL DEFAULT 0,
   elevation REAL DEFAULT 0,
   hike_date INTEGER NOT NULL,
   meeting_place  VARCHAR NOT NULL,
   meeting_time INTEGER NOT NULL,
   mp_latitude REAL NOT NULL,
   mp_longitude REAL NOT NULL,

   at_trailhead_time INTEGER DEFAULT 0,
   home_mp_time INTEGER DEFAULT 0,
   mp_th_time INTEGER DEFAULT 0,
   my_meeting_place  VARCHAR DEFAULT "",
   my_meeting_time INTEGER DEFAULT 0,
   my_mp_latitude REAL DEFAULT 0,
   my_mp_longitude REAL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS  hiker (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   fname VARCHAR NOT NULL,
   lname VARCHAR NOT NULL,
   email VARCHAR NOT NULL UNIQUE
);
/* type 1 - hiker, 10 - primary leader, 11 - assistant leader */
CREATE TABLE IF NOT EXISTS  trip_participant (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   trip_id REFERENCES trip (_id) ON DELETE CASCADE,
   hiker_id REFERENCES hiker (_id),
   type INTEGER DEFAULT 1
 );
CREATE VIEW IF NOT EXISTS trip_roster AS
 SELECT tp.*, p.fname fname, p.lname lname, p.email email
 FROM trip_participant AS tp INNER JOIN hiker AS p
 ON tp.hiker_id = p._id;

CREATE TABLE IF NOT EXISTS plant_trip (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   trip_id REFERENCES trip (_id),
   plant_id INTEGER DEFAULT NULL,
   species_name VARCHAR DEFAULT "",
   species_type INTEGER DEFAULT 1,
   is_leader_list INTEGER DEFAULT 0,
   is_observed INTEGER DEFAULT 0,
   photo_uri VARCHAR DEFAULT "",
   voice_uri VARCHAR DEFAULT "",
   FOREIGN KEY(plant_id) REFERENCES plant_list(_id ),
   CONSTRAINT trip_plant_unique UNIQUE ( trip_id, plant_id ) ON CONFLICT REPLACE
 );

/* check_list: user_set 1 - do not ask any more, type: 1-club, 2-leader, 3-personal */
CREATE TABLE IF NOT EXISTS check_list (
   _id INTEGER PRIMARY KEY AUTOINCREMENT,
   name VARCHAR NOT NULL,
   is_optional INTEGER DEFAULT 0,
   type INTEGER NOT NULL DEFAULT 3,
   trip_id INTEGER DEFAULT NULL,
   is_always_check INTEGER DEFAULT 0,
   is_checked INTEGER DEFAULT 0,
   FOREIGN KEY(trip_id) REFERENCES trip(_id) ON DELETE CASCADE
   );
 CREATE VIEW IF NOT EXISTS club_check_list AS
 SELECT *
 FROM check_list
 WHERE type=1;

 CREATE VIEW IF NOT EXISTS leader_check_list AS
  SELECT *
  FROM check_list
  WHERE type=2;

 CREATE VIEW IF NOT EXISTS my_check_list AS
  SELECT *
  FROM check_list
  WHERE type=3;
