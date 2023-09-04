
CREATE TABLE public.batch_job_instance (
	job_instance_id int8 NOT NULL,
	"version" int8 NULL,
	job_name varchar(100) NOT NULL,
	job_key varchar(32) NOT NULL,
	CONSTRAINT batch_job_instance_pkey PRIMARY KEY (job_instance_id),
	CONSTRAINT job_inst_un UNIQUE (job_name, job_key)
);

CREATE TABLE public.batch_migration_audit_entry (
	id int4 NOT NULL,
	started timestamp NOT NULL,
	modified timestamp NOT NULL,
	status_report text NULL,
	migration_key text NULL,
	batch_size int4 NOT NULL,
	mock_run bool NOT NULL,
	CONSTRAINT batch_migration_audit_entry_pkey PRIMARY KEY (id)
);

CREATE TABLE public.databasechangelog (
	id varchar(255) NOT NULL,
	author varchar(255) NOT NULL,
	filename varchar(255) NOT NULL,
	dateexecuted timestamp NOT NULL,
	orderexecuted int4 NOT NULL,
	exectype varchar(10) NOT NULL,
	md5sum varchar(35) NULL,
	description varchar(255) NULL,
	"comments" varchar(255) NULL,
	tag varchar(255) NULL,
	liquibase varchar(20) NULL,
	contexts varchar(255) NULL,
	labels varchar(255) NULL,
	deployment_id varchar(10) NULL
);


CREATE TABLE public.shedlock (
	name varchar(64) NOT NULL,
	lock_until timestamp(3) NULL,
	locked_at timestamp(3) NULL,
	locked_by varchar(255) NULL,
	CONSTRAINT shedlock_pkey PRIMARY KEY (name)
);

CREATE TABLE public.batch_job_execution (
	job_execution_id int8 NOT NULL,
	"version" int8 NULL,
	job_instance_id int8 NOT NULL,
	create_time timestamp NOT NULL,
	start_time timestamp NULL,
	end_time timestamp NULL,
	status varchar(10) NULL,
	exit_code varchar(2500) NULL,
	exit_message varchar(2500) NULL,
	last_updated timestamp NULL,
	job_configuration_location varchar(2500) NULL,
	CONSTRAINT batch_job_execution_pkey PRIMARY KEY (job_execution_id),
	CONSTRAINT job_inst_exec_fk FOREIGN KEY (job_instance_id) REFERENCES public.batch_job_instance(job_instance_id)
);

CREATE TABLE public.batch_job_execution_context (
	job_execution_id int8 NOT NULL,
	short_context varchar(2500) NOT NULL,
	serialized_context text NULL,
	CONSTRAINT batch_job_execution_context_pkey PRIMARY KEY (job_execution_id),
	CONSTRAINT job_exec_ctx_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);

CREATE TABLE public.batch_job_execution_params (
	job_execution_id int8 NOT NULL,
	type_cd varchar(6) NOT NULL,
	key_name varchar(100) NOT NULL,
	string_val varchar(250) NULL,
	date_val timestamp NULL,
	long_val int8 NULL,
	double_val float8 NULL,
	identifying character(1) NOT NULL,
	CONSTRAINT job_exec_params_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);

CREATE TABLE public.batch_step_execution (
	step_execution_id int8 NOT NULL,
	"version" int8 NOT NULL,
	step_name varchar(100) NOT NULL,
	job_execution_id int8 NOT NULL,
	start_time timestamp NOT NULL,
	end_time timestamp NULL,
	status varchar(10) NULL,
	commit_count int8 NULL,
	read_count int8 NULL,
	filter_count int8 NULL,
	write_count int8 NULL,
	read_skip_count int8 NULL,
	write_skip_count int8 NULL,
	process_skip_count int8 NULL,
	rollback_count int8 NULL,
	exit_code varchar(2500) NULL,
	exit_message varchar(2500) NULL,
	last_updated timestamp NULL,
	CONSTRAINT batch_step_execution_pkey PRIMARY KEY (step_execution_id),
	CONSTRAINT job_exec_step_fk FOREIGN KEY (job_execution_id) REFERENCES public.batch_job_execution(job_execution_id)
);

CREATE TABLE public.batch_step_execution_context (
	step_execution_id int8 NOT NULL,
	short_context varchar(2500) NOT NULL,
	serialized_context text NULL,
	CONSTRAINT batch_step_execution_context_pkey PRIMARY KEY (step_execution_id),
	CONSTRAINT step_exec_ctx_fk FOREIGN KEY (step_execution_id) REFERENCES public.batch_step_execution(step_execution_id)
);

CREATE TABLE public.storeddocument (
	id uuid NOT NULL,
	classification int4 NULL,
	createdby varchar(255) NULL,
	createdon timestamp NULL,
	deleted bool NOT NULL,
	lastmodifiedby varchar(255) NULL,
	modifiedon timestamp NULL,
	ds_idx int4 NULL,
	ttl timestamp NULL,
	harddeleted bool NOT NULL,
	createdbyservice varchar(255) NULL,
	lastmodifiedbyservice varchar(255) NULL,
	CONSTRAINT storeddocumentpk PRIMARY KEY (id),
);

CREATE TABLE public.documentcontentversion (
	id uuid NOT NULL,
	createdby varchar(255) NULL,
	createdon timestamp NULL,
	mimetype varchar(255) NULL,
	originaldocumentname varchar(255) NULL,
	"size" int8 NULL,
	storeddocument_id uuid NULL,
	itm_idx int4 NULL,
	createdbyservice varchar(255) NULL,
	content_uri varchar(512) NULL,
	content_checksum text NULL,
	CONSTRAINT documentcontentversionpk PRIMARY KEY (id),
	CONSTRAINT fk8wq8xkoh1xsmwh2aiisng0vq8 FOREIGN KEY (storeddocument_id) REFERENCES public.storeddocument(id)
);

CREATE TABLE public.documentmetadata (
	documentmetadata_id uuid NOT NULL,
	value varchar(255) NULL,
	name varchar(255) NOT NULL,
	CONSTRAINT documentmetadata_pkey PRIMARY KEY (documentmetadata_id, name),
	CONSTRAINT fkfyhkiy7cd4r4c8p2ifbofpu89 FOREIGN KEY (documentmetadata_id) REFERENCES public.storeddocument(id)
);

CREATE TABLE public.documentroles (
	documentroles_id uuid NOT NULL,
	roles varchar(255) NULL,
	CONSTRAINT fk6y42yshjm9303hkqe8qcabxo7 FOREIGN KEY (documentroles_id) REFERENCES public.storeddocument(id)
);

CREATE TABLE public.auditentry (
	"type" varchar(31) NOT NULL,
	id uuid NOT NULL,
	"action" varchar(255) NULL,
	recordeddatetime timestamp NULL,
	username varchar(255) NULL,
	storeddocument_id uuid NULL,
	documentcontentversion_id uuid NULL,
	servicename varchar(255) NULL,
	CONSTRAINT auditentrypk PRIMARY KEY (id),
	CONSTRAINT fkgyybd8bowfrl5icpi0ddpmxer FOREIGN KEY (storeddocument_id) REFERENCES public.storeddocument(id),
	CONSTRAINT fkoyf06kcby1gjm56bilng4tbkf FOREIGN KEY (documentcontentversion_id) REFERENCES public.documentcontentversion(id)
);

CREATE TABLE public.documentcontent (
	id int8 NOT NULL GENERATED BY DEFAULT AS IDENTITY,
	createdby varchar(255) NULL,
	createdon timestamp NULL,
	lo_data oid NULL,
	documentcontentversion_id uuid NULL,
	"data" bytea NULL,
	CONSTRAINT documentcontentpk PRIMARY KEY (id),
	CONSTRAINT fkky3jly9v67xa62ljxpsbih24b FOREIGN KEY (documentcontentversion_id) REFERENCES public.documentcontentversion(id)
);
