-- SkySimulation default configuration ABI.
-- Java remains the native runtime; Lua owns boot configuration shape.

local gradientPresets = {
    REALISTIC = {
        gradientStyle = "REALISTIC",
        sunsetIntensity = 0.75,
        sunHaloIntensity = 0.75,
        moonHaloIntensity = 0.65
    },
    CINEMATIC = {
        gradientStyle = "CINEMATIC",
        sunsetIntensity = 1.25,
        sunHaloIntensity = 1.15,
        moonHaloIntensity = 1.10
    },
    FANTASY = {
        gradientStyle = "FANTASY",
        sunsetIntensity = 1.80,
        sunHaloIntensity = 1.65,
        moonHaloIntensity = 1.55
    }
}

local activeGradient = gradientPresets.CINEMATIC

local M = {
    schema = "dev.takesome.sky.config.lua.v1",
    module = {
        id = "sky.config",
        version = "1.0.0",
        capabilities = {
            "sky.config.default",
            "sky.atmosphere.profile",
            "sky.atmosphere.gradients",
            "sky.weather.initial",
            "sky.clock.initial",
            "sky.rendering.options",
            "sky.integration.defaults"
        }
    },
    gradientPresets = gradientPresets,
    atmosphere = {
        profile = "Config/skies/earthlike-atmosphere.properties",
        gradientStyle = activeGradient.gradientStyle,
        sunsetIntensity = activeGradient.sunsetIntensity,
        sunHaloIntensity = activeGradient.sunHaloIntensity,
        moonHaloIntensity = activeGradient.moonHaloIntensity
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
