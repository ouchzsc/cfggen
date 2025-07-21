local cfg = require "cfg._cfgs"

local Beans = {}
cfg._beans = Beans

local bean = cfg._mk.bean
local action = cfg._mk.action

---@class Beans.levelrank
---@field level number , 等级
---@field rank number , 品质
---@field RefRank cfg.equip.rank

---@type Beans.levelrank
Beans.levelrank = bean({ 
    { 'RefRank', 0, cfg.equip.rank, 'get', 2 }, }, 
    'level', -- int, 等级
    'rank' -- int, 品质
    )

---@class Beans.position
---@field x number 
---@field y number 
---@field z number 

---@type Beans.position
Beans.position = bean(nil, 
    'x', -- int
    'y', -- int
    'z' -- int
    )

---@class Beans.range
---@field min number , 最小
---@field max number , 最大

---@type Beans.range
Beans.range = bean(nil, 
    'min', -- int, 最小
    'max' -- int, 最大
    )

Beans.ai = {}
---@class Beans.ai.triggertick

---@type Beans.ai.triggertick
Beans.ai.triggertick = {}

---@class Beans.ai.triggertick.constvalue : Beans.ai.triggertick
---@field value number 

---@type Beans.ai.triggertick.constvalue
Beans.ai.triggertick.constvalue = action("ConstValue", nil, 
    'value' -- int
    )

---@class Beans.ai.triggertick.bylevel : Beans.ai.triggertick
---@field init number 
---@field coefficient number 

---@type Beans.ai.triggertick.bylevel
Beans.ai.triggertick.bylevel = action("ByLevel", nil, 
    'init', -- int
    'coefficient' -- float
    )

---@class Beans.ai.triggertick.byserverupday : Beans.ai.triggertick
---@field init number 
---@field coefficient1 number 
---@field coefficient2 number 

---@type Beans.ai.triggertick.byserverupday
Beans.ai.triggertick.byserverupday = action("ByServerUpDay", nil, 
    'init', -- int
    'coefficient1', -- float
    'coefficient2' -- float
    )

Beans.equip = {}
---@class Beans.equip.testpackbean
---@field name string 
---@field iRange Beans.range 

---@type Beans.equip.testpackbean
Beans.equip.testpackbean = bean(nil, 
    'name', -- str
    'iRange' -- Range
    )

Beans.other = {}
---@class Beans.other.dropitem
---@field chance number , 掉落概率
---@field itemids table<number,number> , 掉落物品
---@field countmin number , 数量下限
---@field countmax number , 数量上限

---@type Beans.other.dropitem
Beans.other.dropitem = bean(nil, 
    'chance', -- int, 掉落概率
    'itemids', -- list<int>, 掉落物品
    'countmin', -- int, 数量下限
    'countmax' -- int, 数量上限
    )

Beans.task = {}
---@class Beans.task.testdefaultbean
---@field testInt number 
---@field testBool boolean 
---@field testString string 
---@field testSubBean Beans.position 
---@field testList table<number,number> 
---@field testList2 table<number,number> 
---@field testMap table<number,string> 

---@type Beans.task.testdefaultbean
Beans.task.testdefaultbean = bean(nil, 
    'testInt', -- int
    'testBool', -- bool
    'testString', -- str
    'testSubBean', -- Position
    'testList', -- list<int>
    'testList2', -- list<int>
    'testMap' -- map<int,str>
    )

---@class Beans.task.completecondition

---@type Beans.task.completecondition
Beans.task.completecondition = {}

---@class Beans.task.completecondition.killmonster : Beans.task.completecondition
---@field monsterid number 
---@field count number 
---@field RefMonsterid cfg.other.monster

---@type Beans.task.completecondition.killmonster
Beans.task.completecondition.killmonster = action("KillMonster", { 
    { 'RefMonsterid', 0, cfg.other.monster, 'get', 1 }, }, 
    'monsterid', -- int
    'count' -- int
    )

---@class Beans.task.completecondition.talknpc : Beans.task.completecondition
---@field npcid number 

---@type Beans.task.completecondition.talknpc
Beans.task.completecondition.talknpc = action("TalkNpc", nil, 
    'npcid' -- int
    )

---@class Beans.task.completecondition.testnocolumn : Beans.task.completecondition

---@type Beans.task.completecondition.testnocolumn
Beans.task.completecondition.testnocolumn = action("TestNoColumn")()

---@class Beans.task.completecondition.chat : Beans.task.completecondition
---@field msg string 

---@type Beans.task.completecondition.chat
Beans.task.completecondition.chat = action("Chat", nil, 
    'msg' -- str
    )

---@class Beans.task.completecondition.conditionand : Beans.task.completecondition
---@field cond1 Beans.task.completecondition 
---@field cond2 Beans.task.completecondition 

---@type Beans.task.completecondition.conditionand
Beans.task.completecondition.conditionand = action("ConditionAnd", nil, 
    'cond1', -- task.completecondition
    'cond2' -- task.completecondition
    )

---@class Beans.task.completecondition.collectitem : Beans.task.completecondition
---@field itemid number 
---@field count number 

---@type Beans.task.completecondition.collectitem
Beans.task.completecondition.collectitem = action("CollectItem", nil, 
    'itemid', -- int
    'count' -- int
    )


return Beans
