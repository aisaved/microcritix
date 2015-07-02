
CREATE TABLE country(
       country_id serial PRIMARY KEY,
       country_name VARCHAR (255) UNIQUE NOT NULL,
       country_code VARCHAR (5) UNIQUE NOT NULL);


CREATE TABLE user_account(
       user_account_id serial PRIMARY KEY,
       username VARCHAR (355) UNIQUE NOT NULL,
       password VARCHAR (1024) NOT NULL,
       email VARCHAR (355) UNIQUE NOT NULL,
       created_on TIMESTAMP NOT NULL,
       is_admin BOOLEAN NOT NULL default false,
       active BOOLEAN NOT NULL default false,
       last_login TIMESTAMP);

CREATE TABLE user_registration(
       user_registration_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       registration_key VARCHAR (255),
       activated BOOLEAN NOT NULL default false,
       CONSTRAINT user_registration_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE user_profile(
       user_profile_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       first_name VARCHAR (100),
       middle_name VARCHAR (100),
       last_name VARCHAR (100),
       address_line_1 VARCHAR (255),
       address_line_2 VARCHAR (255),
       city VARCHAR (255),
       state_province VARCHAR (255),
       country_code VARCHAR (3),
       CONSTRAINT user_profile_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

CREATE TABLE user_session(
       user_session_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       auth_token VARCHAR(64),
       session_expiry TIMESTAMP NOT NULL,
       CONSTRAINT user_session_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE user_password_reset(
       user_password_reset_id serial PRIMARY KEY,
       user_account_id integer NOT NULL,
       password_reset_key VARCHAR(32),
       key_expiry TIMESTAMP NOT NULL,
       CONSTRAINT user_password_reset_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE site_settings(
       site_settings_id serial PRIMARY KEY,
       active BOOLEAN NOT NULL DEFAULT false,
       site_name VARCHAR (255) NOT NULL,
       site_domain_name VARCHAR (1024) UNIQUE NOT NULL,
       site_link VARCHAR(1024),
       site_email VARCHAR(255),
       site_phone VARCHAR (50),
       enable_email BOOLEAN NOT NULL DEFAULT true,
       mail_host VARCHAR (255),
       mail_user VARCHAR (255),
       mail_password VARCHAR (255),
       mail_port integer,
       mail_ssl BOOLEAN NOT NULL DEFAULT false,
       mail_api_url VARCHAR (255),
       mail_option VARCHAR (10),
       template_folder VARCHAR (255),
       facebook_client_id VARCHAR(100)
       );



CREATE TABLE page(
       page_id serial PRIMARY KEY,
       page_title VARCHAR (1024) NOT NULL,
       page_content text,
       page_content_html text,
       page_meta_keywords VARCHAR (255),
       page_meta_description text,
       page_url VARCHAR (2048),
       site_settings_id integer NOT NULL,
       page_active BOOLEAN NOT NULL DEFAULT true,
       page_template VARCHAR (255) NOT NULL,
       CONSTRAINT page_site_settings_id_fkey FOREIGN KEY (site_settings_id)
       REFERENCES site_settings (site_settings_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );
CREATE INDEX page_url_index on page (page_url);


CREATE TABLE job(
       job_id serial PRIMARY KEY,
       job_title VARCHAR (1024) NOT NULL,
       job_type VARCHAR (50) NOT NULL,
       job_location VARCHAR (1024) NOT NULL,
       job_description text,
       job_how_to_apply text,
       job_who_can_apply VARCHAR (50) NOT NULL default 'premium',
       job_company_name VARCHAR (255) NOT NULL default 'Not specified',
       job_company_location VARCHAR (255) NOT NULL default 'Not specified',
       job_budget DECIMAL(8,2) NOT NULL DEFAULT 0.0,
       job_budget_interval VARCHAR (30),
       job_tags text,
       job_active BOOLEAN NOT NULL DEFAULT false,
       job_created_date TIMESTAMP NOT NULL,
       job_published_date TIMESTAMP NULL,
       job_updated_date TIMESTAMP NOT NULL,
       job_expiry_date TIMESTAMP NOT NULL,
       job_applicants_count integer DEFAULT 0,
       job_status varchar(50) NOT NULL,
       job_url_slug varchar(1024),
       );

CREATE TABLE job_tag(
       job_tag_id serial PRIMARY KEY,
       job_id integer,
       job_tag_name varchar (100),
       CONSTRAINT job_tag_job_id_fkey FOREIGN KEY (job_id)
       REFERENCES job (job_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE job_editor(
       job_editor_id serial PRIMARY KEY,
       user_account_id integer,
       job_id integer,
       CONSTRAINT job_editor_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE,
       CONSTRAINT job_editor_job_id_fkey FOREIGN KEY (job_id)
       REFERENCES job (job_id) MATCH SIMPLE 
       ON DELETE CASCADE);


CREATE TABLE job_applicant(
       job_applicant_id serial PRIMARY KEY,
       user_account_id integer,
       job_id integer,
       cover_letter text,
       CONSTRAINT job_applicant_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE,
       CONSTRAINT job_applicant_job_id_fkey FOREIGN KEY (job_id)
       REFERENCES job (job_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

CREATE TABLE job_raw_application(
       job_raw_application_id serial PRIMARY KEY,
       job_id integer,
       cover_letter text,
       resume text,
       CONSTRAINT job_raw_application_job_id_fkey FOREIGN KEY (job_id)
       REFERENCES job (job_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );

CREATE TABLE job_credits(
       user_account_id integer,
       job_credits integer NOT NULL DEFAULT 0,
       CONSTRAINT job_credits_user_account_id_fkey FOREIGN KEY (user_account_id)
       REFERENCES user_account (user_account_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );


CREATE TABLE job_profile(
       job_profile_id integer,
       job_profile_title varchar 1024,
       job_profile_description text,
       job_profile_tags text
       
       )
