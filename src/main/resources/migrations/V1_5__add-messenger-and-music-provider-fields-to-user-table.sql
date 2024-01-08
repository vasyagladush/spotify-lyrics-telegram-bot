ALTER TABLE public.users ADD COLUMN messenger_platform public.messenger_platform_enum_type NOT NULL;
ALTER TABLE public.users ADD COLUMN music_provider_platform public.music_provider_platform_enum_type;
ALTER TABLE public.users ADD COLUMN music_provider_access_token character varying;
ALTER TABLE public.users ADD COLUMN music_provider_refresh_token character varying;
