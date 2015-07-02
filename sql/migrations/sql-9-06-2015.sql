ALTER TABLE jobs add column job_tags text;
CREATE TABLE job_tag(
       job_tag_id serial PRIMARY KEY,
       job_id integer,
       job_tag_name varchar (100),
       CONSTRAINT job_tag_job_id_fkey FOREIGN KEY (job_id)
       REFERENCES job (job_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

