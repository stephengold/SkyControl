-- SkySimulation command ABI manifest.
-- Java executes commands through SkyCommandBus; Lua declares command contract.

local M = {
    schema = "dev.takesome.sky.commands.lua.v1",
    module = {
        id = "sky.commands",
        version = "1.0.0",
        capabilities = {
            "sky.command.bus",
            "sky.weather.commands",
            "sky.clock.commands",
            "sky.environment.commands",
            "sky.config.commands"
        }
    },
    commands = {
        ["sky.weather.set"] = {
            args = {"weatherId", "transitionSeconds?"},
            updates = {"sky.weather.current", "sky.environment"},
            events = {"sky.weather.changed", "sky.environment.changed"}
        },
        ["sky.weather.list"] = {
            args = {},
            returns = {"weatherIds"}
        },
        ["sky.clock.setTime"] = {
            args = {"hour"},
            updates = {"sky.clock.hour", "sky.environment"},
            events = {"sky.clock.changed", "sky.environment.changed"}
        },
        ["sky.clock.advance"] = {
            args = {"seconds", "secondsPerDay"},
            updates = {"sky.clock.hour", "sky.environment"},
            events = {"sky.clock.changed", "sky.environment.changed"}
        },
        ["sky.environment.snapshot"] = {
            args = {},
            returns = {"sky.environment.snapshot"}
        },
        ["sky.config.reload"] = {
            args = {},
            updates = {
                "sky.config",
                "sky.weather.registry",
                "sky.environment"
            },
            events = {"sky.config.reloaded", "sky.environment.changed"}
        }
    }
}

return M
