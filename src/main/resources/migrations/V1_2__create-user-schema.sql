CREATE TABLE IF NOT EXISTS public.users (
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    messenger_user_id character varying NOT NULL,
    CONSTRAINT "PK_29708a53e178d1c04d542a81800" PRIMARY KEY ("id")
);
