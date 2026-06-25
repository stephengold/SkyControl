-- SkySimulation default configuration ABI.
-- Java remains the native runtime; Lua owns boot configuration shape.

local M = {
    schema = "dev.takesome.sky.config.lua.v1",
    module = {
        id = "sky.config",
        version = "1.0.0",
        capabilities = {
            "sky.config.default",
            "sky.atmosphere.profile",
            "sky.weather.initial",
            "sky.clock.initial",
            "sky.rendering.options",
            "sky.integration.defaults"
        }
    },
    atmosphere = {
        profile = "Config/skies/earthlike-atmosphere.properties"
    },
    weather = {
        registry = "helix/lua/sky/weather.lua",
        initial = "FAIR",
        transitionSeconds = 45.0
    },
    clock = {
        hour = 12.0,
        observerLatitudeDegrees = 51.1788,
        solarMonth = 6,
        solarDay = 21
    },
    rendering = {
        stars = "TwoDomes",
        cloudFlattening = 0.9,
        cloudsYOffset = 0.4,
        lowerDome = true
    },
    integration = {
        cloudModulation = true
    }
}

return M
