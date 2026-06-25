-- SkySimulation Lua Weather ABI.
-- Java remains the native renderer/runtime. Lua owns preset data and ABI shape.

local M = {
    schema = "dev.takesome.sky.weather.lua.v1",
    module = {
        id = "sky.weather",
        version = "1.0.0",
        capabilities = {
            "sky.weather.registry",
            "sky.weather.presets",
            "sky.environment.state"
        },
        commands = {
            "sky.weather.set",
            "sky.weather.list",
            "sky.weather.snapshot"
        },
        state = {
            "sky.weather.current",
            "sky.environment.visibility",
            "sky.environment.precipitation",
            "sky.environment.wind"
        },
        events = {
            "sky.weather.changed",
            "sky.environment.changed"
        }
    },
    defaults = {
        transitionSeconds = 60.0,
        maxCloudLayers = 6
    },
    presets = {}
}

local C = "Textures/skies/clouds"
local P = C .. "/presets"

local function layer(alphaMap, normalMap, opacity, scale, uRate, vRate)
    return {
        alphaMap = alphaMap,
        normalMap = normalMap,
        opacity = opacity,
        scale = scale,
        uRate = uRate,
        vRate = vRate
    }
end

local function world(cloudiness, visibility, precipitation,
        windStrength, lightningChance)
    return {
        cloudiness = cloudiness,
        visibility = visibility,
        precipitation = precipitation,
        windStrength = windStrength,
        lightningChance = lightningChance
    }
end

M.presets.CLEAR = {
    description = "No visible clouds; legacy clear fallback.",
    transitionSeconds = 20.0,
    world = world(0.0, 1.0, 0.0, 0.05, 0.0),
    layers = {
        layer(C .. "/clear.png", nil, 0.0, 1.0, 0.0, 0.0)
    }
}

M.presets.FAIR = {
    description = "Fair weather with two FBM cloud layers.",
    transitionSeconds = 45.0,
    world = world(0.35, 0.95, 0.0, 0.10, 0.0),
    layers = {
        layer(C .. "/fbm.png", nil, 0.35, 1.50, -0.0005, 0.0030),
        layer(C .. "/fbm.png", nil, 0.18, 2.15, 0.0003, 0.0010)
    }
}

M.presets.OVERCAST = {
    description = "Broad overcast fallback with FBM detail.",
    transitionSeconds = 60.0,
    world = world(0.72, 0.75, 0.0, 0.25, 0.0),
    layers = {
        layer(C .. "/overcast.png", nil, 0.72, 1.00, 0.0, 0.0003),
        layer(C .. "/fbm.png", nil, 0.20, 2.40, 0.0002, 0.0010)
    }
}

M.presets.WISPY = {
    description = "Thin high clouds with cirrocumulus and wisps.",
    transitionSeconds = 60.0,
    world = world(0.28, 0.92, 0.0, 0.20, 0.0),
    layers = {
        layer(P .. "/wispy/skyhat_cirrocumulus01_ap.dds",
            P .. "/wispy/skyhat_cirrocumulus01_nrm.dds",
            0.28, 1.30, 0.0001, 0.0012),
        layer(P .. "/wispy/wisps_ap.dds",
            P .. "/wispy/wisps_nrm.dds",
            0.16, 2.20, -0.0002, 0.0020)
    }
}

M.presets.CLOUDY = {
    description = "Medium coverage with detail modulation.",
    transitionSeconds = 60.0,
    world = world(0.55, 0.78, 0.0, 0.35, 0.0),
    layers = {
        layer(P .. "/cloudy/trialap.dds",
            P .. "/cloudy/trialn.dds",
            0.52, 1.20, -0.0003, 0.0018),
        layer(P .. "/cloudy/cloudhat_marble02_ap.dds",
            P .. "/cloudy/detail1_nrm.dds",
            0.22, 3.00, 0.0004, 0.0024)
    }
}

M.presets.RAIN = {
    description = "Rain clouds with cloudy base and fast detail.",
    transitionSeconds = 75.0,
    world = world(0.75, 0.55, 0.75, 0.55, 0.02),
    layers = {
        layer(P .. "/rain/skyhat_rain02_ap.dds",
            P .. "/rain/skyhat_rain02_n2.dds",
            0.68, 1.10, -0.0003, 0.0026),
        layer(P .. "/cloudy/trialap.dds",
            P .. "/cloudy/trialn.dds",
            0.35, 2.00, 0.0002, 0.0018),
        layer(P .. "/cloudy/cloudhat_marble02_ap.dds",
            P .. "/cloudy/detail1_nrm.dds",
            0.18, 3.50, 0.0004, 0.0030)
    }
}

M.presets.STORM = {
    description = "Heavy storm front, rain veil, and high-frequency detail.",
    transitionSeconds = 90.0,
    world = world(0.90, 0.35, 0.95, 0.90, 0.20),
    layers = {
        layer(P .. "/storm/stormclouds_ap.dds",
            P .. "/storm/stormclouds_nrm.dds",
            0.85, 1.00, -0.0004, 0.0032),
        layer(P .. "/storm/skyhat_rain02_ap.dds",
            P .. "/storm/skyhat_rain02_n2.dds",
            0.42, 1.80, 0.0002, 0.0026),
        layer(P .. "/storm/cloudhat_marble02_ap.dds",
            P .. "/storm/detail1_nrm.dds",
            0.26, 4.00, 0.0005, 0.0035)
    }
}

M.presets.NIMBUS = {
    description = "Nimbus coverage with detail modulation.",
    transitionSeconds = 75.0,
    world = world(0.82, 0.45, 0.80, 0.70, 0.08),
    layers = {
        layer(P .. "/nimbus/final_nimbusclouds_ap.dds",
            P .. "/nimbus/final_nimbusclouds_n.dds",
            0.76, 1.10, -0.0004, 0.0020),
        layer(P .. "/nimbus/cloudhat_marble02_ap.dds",
            P .. "/nimbus/detail1_nrm.dds",
            0.22, 3.50, 0.0003, 0.0024)
    }
}

return M
