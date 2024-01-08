ALTER TABLE public.users ADD COLUMN autoclear boolean NOT NULL DEFAULT true;
ALTER TABLE public.users ADD COLUMN messages_to_clear character varying;