DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'music_provider_platform_enum_type') THEN
        create type music_provider_platform_enum_type AS ENUM ('SPOTIFY');
    END IF;
END
$$;