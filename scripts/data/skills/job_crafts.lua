-- Craft & Smithmagic skills of jobs that have no gathering script.
--
-- Skill IDs and tools come from the `jobs_data` table (columns `skills` and `tools`).
-- Craft skills of gathering jobs live in their own file and are NOT repeated here:
--   101 (lumberjack), 32/48 (miner), 23 (alchemist), 47/122 (farmer), 133 (fisherman).
--
-- Recipes themselves are resolved in Java from `jobs_data.crafts` + the crafts table.

---@type {jobID:number, skills:number[], tools:number[]}[]
local craftJobs = {
    -- Food
    { jobID = BakerJob,           skills = {27, 109},       tools = {492} },
    { jobID = HunterJob,          skills = {132},           tools = {1934} },
    { jobID = ButcherJob,         skills = {134},           tools = {1945} },
    { jobID = FishmongerJob,      skills = {135},           tools = {1946} },

    -- Smiths
    { jobID = SwordSmithJob,      skills = {20, 145},       tools = {494} },
    { jobID = DaggerSmithJob,     skills = {18, 142},       tools = {495} },
    { jobID = HammerSmithJob,     skills = {19, 144},       tools = {493} },
    { jobID = ShovelSmithJob,     skills = {21, 146},       tools = {496} },
    { jobID = AxeSmithJob,        skills = {65, 143},       tools = {922} },
    { jobID = ShieldSmithJob,     skills = {156},           tools = {7098} },

    -- Carvers
    { jobID = BowCarverJob,       skills = {15, 149},       tools = {500} },
    { jobID = WandCarverJob,      skills = {16, 148},       tools = {499} },
    { jobID = StaffCarverJob,     skills = {17, 147},       tools = {498} },

    -- Tailor, Shoemaker, Jeweller, Handyman
    { jobID = TailorJob,          skills = {63, 64, 123},   tools = {951} },
    { jobID = ShoemakerJob,       skills = {13, 14},        tools = {579} },
    { jobID = JewellerJob,        skills = {11, 12},        tools = {491} },
    { jobID = HandymanJob,        skills = {171, 182},      tools = {7650} },

    -- Smithmagic
    { jobID = DaggerSmithmagusJob, skills = {1},            tools = {1520} },
    { jobID = SwordSmithmagusJob,  skills = {113},          tools = {1539} },
    { jobID = HammerSmithmagusJob, skills = {116},          tools = {1561} },
    { jobID = ShovelSmithmagusJob, skills = {117},          tools = {1560} },
    { jobID = AxeSmithmagusJob,    skills = {115},          tools = {1562} },
    { jobID = BowCarvmagusJob,     skills = {118},          tools = {1563} },
    { jobID = WandCarvmagusJob,    skills = {119},          tools = {1564} },
    { jobID = StaffCarvmagusJob,   skills = {120},          tools = {1565} },
    { jobID = ShoemagusJob,        skills = {163, 164},     tools = {7495} },
    { jobID = JewelmagusJob,       skills = {168, 169},     tools = {7493} },
    { jobID = CostumagusJob,       skills = {165, 166, 167}, tools = {7494} },
}

for _, job in ipairs(craftJobs) do
    for _, skillID in ipairs(job.skills) do
        registerCraftSkill(skillID, {jobID = job.jobID, toolIDs = job.tools})
    end
end

-- Crafts that need no job (Java looks the recipes up in jobs_data by skill id)
local function fixedSlots(n) return function(_) return n end end

registerCraftSkill(22,  nil, fixedSlots(8)) -- Potato table
registerCraftSkill(110, nil, fixedSlots(8)) -- Wood bench
registerCraftSkill(121, nil, fixedSlots(8)) -- Crusher (Munster)
registerCraftSkill(151, nil, fixedSlots(8)) -- Pyrotechnics workbench
