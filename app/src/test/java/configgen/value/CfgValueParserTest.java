package configgen.value;

import configgen.Resources;
import configgen.ctx.Context;
import configgen.data.CfgData;
import configgen.schema.InterfaceSchema;
import configgen.schema.TableSchema;
import configgen.value.CfgValue.*;
import configgen.value.CfgValueErrs.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CfgValueParserTest {

    private @TempDir Path tempDir;

    @Test
    void parseCfgValue_simple() {
        String cfgStr = """
                table rank[RankID] (enum='RankName'){
                    [RankName];
                    RankID:int; // 稀有度
                    RankName:str; // 程序用名字
                    RankShowName:text; // 显示名称
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        Resources.addTempFileFromResourceFile("rank.csv", tempDir);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable rank = cfgValue.getTable("rank");
        TableSchema rankSchema = ctx.cfgSchema().findTable("rank");

        assertEquals(rankSchema, rank.schema());
        assertEquals(5, rank.valueList().size());
        {
            VStruct v = rank.valueList().getFirst();
            assertEquals(rankSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            assertEquals("white", ((VString) v.values().get(1)).value());
            assertEquals("下品", ((VText) v.values().get(2)).value());
        }
        {
            VStruct v = rank.valueList().get(4);
            assertEquals(rankSchema, v.schema());
            assertEquals(5, ((VInt) v.values().getFirst()).value());
            assertEquals("yellow", ((VString) v.values().get(1)).value());
            assertEquals("准神", ((VText) v.values().get(2)).value());
        }
    }

    @Test
    void parseCfgValue_strLowercase() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    s:str(lowercase);
                    s2:text(lowercase);
                    s3:list<str>(lowercase, fix=1);
                    s4:map<int,str>(lowercase, fix=1);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,s,s2,s3,s4.k,s4.v
                1,ABc,ABCD,AA,11,VV
                2,中文A,中文B,,,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();

            VString s = (VString) v.values().get(1);
            assertEquals("abc", s.value());
            VText s2 = (VText) v.values().get(2);
            assertEquals("abcd", s2.value());

            VList s3 = (VList) v.values().get(3);
            assertEquals("aa", ((VString)s3.valueList().getFirst()).value());

            VMap s4 = (VMap) v.values().get(4);
            Map.Entry<SimpleValue, SimpleValue> e = s4.valueMap().entrySet().iterator().next();
            assertEquals("vv", ((VString)e.getValue()).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VString s = (VString) v.values().get(1);
            assertEquals("中文a", s.value());
            VText s2 = (VText) v.values().get(2);
            assertEquals("中文b", s2.value());
        }
    }

    @Test
    void parseCfgValue_struct() {
        String cfgStr = """
                struct s {
                    a:int;
                    b:int;
                }
                table t[id] {
                    id:int;
                    s:s;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,s.a,s.b
                1,111,222
                2,333,444""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();
            assertEquals(tSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            VStruct s = (VStruct) v.values().get(1);
            assertEquals(ctx.cfgSchema().findItem("s"), s.schema());
            assertEquals(111, ((VInt) s.values().getFirst()).value());
            assertEquals(222, ((VInt) s.values().get(1)).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VStruct s = (VStruct) v.values().get(1);
            assertEquals(333, ((VInt) s.values().getFirst()).value());
            assertEquals(444, ((VInt) s.values().get(1)).value());
        }
    }


    @Test
    void parseCfgValue_interface() {
        String cfgStr = """
                interface action {
                    struct cast{
                        skillId:int;
                    }
                    struct talk{
                        talkId:int;
                        str:text;
                    }
                }
                table t[id] {
                    id:int;
                    action:action;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,action.name,action.param1,action.param2
                1,cast,123,
                2,talk,111,hello world!""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");
        InterfaceSchema action = (InterfaceSchema) ctx.cfgSchema().findItem("action");


        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();
            assertEquals(tSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            VInterface s = (VInterface) v.values().get(1);
            assertEquals(action, s.schema());
            assertEquals(action.findImpl("cast"), s.child().schema());
            assertEquals(123, ((VInt) s.child().values().getFirst()).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VInterface s = (VInterface) v.values().get(1);
            assertEquals(111, ((VInt) s.child().values().getFirst()).value());
            assertEquals("hello world!", ((VText) s.child().values().get(1)).value());
        }
    }


    @Test
    void parseCfgValue_list() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    intList:list<int> (fix=3);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,
                id,intList.1,intList.2,intList.3
                1,111,222,333
                2,333,444,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();
            assertEquals(tSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(3, s.valueList().size());
            assertEquals(111, ((VInt) s.valueList().getFirst()).value());
            assertEquals(222, ((VInt) s.valueList().get(1)).value());
            assertEquals(333, ((VInt) s.valueList().get(2)).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(333, ((VInt) s.valueList().getFirst()).value());
            assertEquals(444, ((VInt) s.valueList().get(1)).value());
        }
    }

    @Test
    void parseCfgValue_listPack() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    intList:list<int> (pack);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,intList
                1,"111,222,333"
                2,"333,444"
                3,""
                """;

        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(3, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();
            assertEquals(tSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(3, s.valueList().size());
            assertEquals(111, ((VInt) s.valueList().getFirst()).value());
            assertEquals(222, ((VInt) s.valueList().get(1)).value());
            assertEquals(333, ((VInt) s.valueList().get(2)).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(333, ((VInt) s.valueList().getFirst()).value());
            assertEquals(444, ((VInt) s.valueList().get(1)).value());
        }
        {
            VStruct v = tVTable.valueList().get(2);
            assertEquals(3, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(0, s.valueList().size());
        }
    }


    @Test
    void parseCfgValue_listBlock() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    intList:list<int> (block=3);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,
                id,intList.1,intList.2,intList.3
                1,111,222,333
                ,444,555,666
                ,777,,
                2,123,,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct v = tVTable.valueList().getFirst();
            assertEquals(tSchema, v.schema());
            assertEquals(1, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(7, s.valueList().size());
            assertEquals(111, ((VInt) s.valueList().getFirst()).value());
            assertEquals(777, ((VInt) s.valueList().get(6)).value());
        }
        {
            VStruct v = tVTable.valueList().get(1);
            assertEquals(2, ((VInt) v.values().getFirst()).value());
            VList s = (VList) v.values().get(1);
            assertEquals(1, s.valueList().size());
            assertEquals(123, ((VInt) s.valueList().getFirst()).value());
        }
    }

    @Test
    void parseCfgValue_nestedListBlock() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    rewardList:list<Reward> (block=1);
                }
                
                struct Reward {
                    rewardName:text;
                    chestList:list<Chest> (block=1);
                }
                
                struct Chest {
                    chestName:text;
                    itemList:list<Item> (block=1);
                }
                
                struct Item {
                    itemId:int;
                    name:text;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,
                id,rewardList.rewardName,chestName,itemId,itemName
                1,地狱熔炉奖励,罗格的宝箱,30001,末日毁灭者
                ,,,30002,风之力
                ,,血鸟宝箱,30010,法力药水
                ,,,30011,暴风之盾
                2,安雅的奖励,普通宝箱,30021,乔丹之石
                ,,,30022,彩虹刻面""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValue cfgValue = ctx.makeValue();
        VTable tVTable = cfgValue.getTable("t");
        TableSchema tSchema = ctx.cfgSchema().findTable("t");

        assertEquals(tSchema, tVTable.schema());
        assertEquals(2, tVTable.valueList().size());
        {
            VStruct first = tVTable.valueList().getFirst();
            VStruct firstReward = (VStruct) ((VList)(first.values().get(1))).valueList().get(0);
            assertEquals("1,((地狱熔炉奖励,((罗格的宝箱,((30001,末日毁灭者),(30002,风之力))),(血鸟宝箱,((30010,法力药水),(30011,暴风之盾))))))", first.packStr());

            VText rewardName = (VText) firstReward.values().getFirst();
            assertEquals("地狱熔炉奖励",rewardName.value());
            VList chestList = (VList) firstReward.values().get(1);
            assertEquals(2, chestList.valueList().size());
            VStruct chest1 = (VStruct) chestList.valueList().getFirst();
            VText chestName = (VText) chest1.values().getFirst();
            assertEquals("罗格的宝箱", chestName.value());
            VList itemList = (VList) chest1.values().get(1);
            assertEquals(2, itemList.valueList().size());
            VStruct item2 = (VStruct) itemList.valueList().get(1);
            assertEquals(30002, ((VInt)item2.values().getFirst()).value());
            assertEquals("风之力", ((VText)item2.values().get(1)).value());

            VStruct chest2 = (VStruct) chestList.valueList().getFirst();
            VList itemList2 = (VList) chest2.values().get(1);
            assertEquals(2, itemList2.valueList().size());
        }
        {
            VStruct second = tVTable.valueList().get(1);
            VStruct firstReward = (VStruct) ((VList)(second.values().get(1))).valueList().get(0);
            assertEquals("2,((安雅的奖励,((普通宝箱,((30021,乔丹之石),(30022,彩虹刻面))))))", second.packStr());

            VText rewardName = (VText) firstReward.values().getFirst();
            assertEquals("安雅的奖励",rewardName.value());
            VList chestList = (VList) firstReward.values().get(1);
            assertEquals(1, chestList.valueList().size());
            VStruct chest1 = (VStruct) chestList.valueList().getFirst();
            VText chestName = (VText) chest1.values().getFirst();
            assertEquals("普通宝箱", chestName.value());
            VList itemList = (VList) chest1.values().get(1);
            assertEquals(2, itemList.valueList().size());
        }
    }


    @Test
    void error_InterfaceCellEmptyButHasNoDefaultImpl() {
        String cfgStr = """
                interface action {
                    struct cast{
                        skillId:int;
                    }
                    struct talk{
                        talkId:int;
                        str:text;
                    }
                }
                table t[id] {
                    id:int;
                    action:action;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,action.name,action.param1,action.param2
                1,,,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(InterfaceCellEmptyButHasNoDefaultImpl.class, valueErrs.errs().getFirst());
        InterfaceCellEmptyButHasNoDefaultImpl err = (InterfaceCellEmptyButHasNoDefaultImpl) valueErrs.errs().getFirst();
        assertEquals("action", err.interfaceName());
        assertTrue(err.source() instanceof CfgData.DCell cell &&
                cell.rowId().row() == 2 &&
                cell.col() == 1);

    }


    @Test
    void error_InterfaceCellImplNotFound() {
        String cfgStr = """
                interface action {
                    struct cast{
                        skillId:int;
                    }
                    struct talk{
                        talkId:int;
                        str:text;
                    }
                }
                table t[id] {
                    id:int;
                    action:action;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,action.name,action.param1,action.param2
                1,notExistAction,,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(InterfaceCellImplNotFound.class, valueErrs.errs().getFirst());
        InterfaceCellImplNotFound err = (InterfaceCellImplNotFound) valueErrs.errs().getFirst();
        assertEquals("action", err.interfaceName());
        assertEquals("notExistAction", err.notFoundImpl());
        assertTrue(err.source() instanceof CfgData.DCell cell &&
                cell.rowId().row() == 2 &&
                cell.col() == 1);
    }


    @Test
    void error_FieldCellSpanNotEnough() {
        String cfgStr = """
                interface action {
                    struct cast{
                        skillId:int;
                    }
                    struct talk{
                        talkId:int;
                        str:text;
                    }
                }
                table t[id] {
                    id:int;
                    action:action (pack);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,action
                1,talk(123)""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();
        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(FieldCellSpanNotEnough.class, valueErrs.errs().getFirst());
        FieldCellSpanNotEnough err = (FieldCellSpanNotEnough) valueErrs.errs().getFirst();
        assertEquals("talk", err.nameable());
        assertEquals("str", err.field());
    }


    @Test
    void error_NotMatchFieldType() {
        String cfgStr = """
                struct s {
                    a:int;
                    b:int;
                }
                table t[id] {
                    id:int;
                    s:s;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,
                id,s.a,s.b
                1,111,abc""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(NotMatchFieldType.class, valueErrs.errs().getFirst());
        NotMatchFieldType err = (NotMatchFieldType) valueErrs.errs().getFirst();
        assertEquals("s", err.nameable());
        assertEquals("b", err.field());
    }

    @Test
    void error_MapKeyDuplicated() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    s:map<int,str> (fix=2);
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,,
                id,s.k1,s.v1,s.k2,v2
                1,111,abc,111,efg""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(MapKeyDuplicated.class, valueErrs.errs().getFirst());
        MapKeyDuplicated err = (MapKeyDuplicated) valueErrs.errs().getFirst();
        assertEquals("t", err.nameable());
        assertEquals("s", err.field());
    }

    @Test
    void error_PrimaryOrUniqueKeyDuplicated() {
        String cfgStr = """
                table t[id] {
                    id:int;
                    s:text;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,,
                id,s
                1,abc
                1,efg""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(PrimaryOrUniqueKeyDuplicated.class, valueErrs.errs().getFirst());
        PrimaryOrUniqueKeyDuplicated err = (PrimaryOrUniqueKeyDuplicated) valueErrs.errs().getFirst();
        assertEquals("t", err.table());
        assertEquals(List.of("id"), err.keys());
        assertEquals(1, ((VInt) err.value()).value());
    }

    @Test
    void error_EnumEmpty() {
        String cfgStr = """
                table t[id] (enum='s') {
                    id:int;
                    s:str;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,,
                id,s
                1,red
                2,""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(EnumEmpty.class, valueErrs.errs().getFirst());
        EnumEmpty err = (EnumEmpty) valueErrs.errs().getFirst();
        assertEquals("t", err.table());
        assertTrue(err.source() instanceof CfgData.DCell cell &&
                cell.rowId().row() == 3 &&
                cell.col() == 1);
    }


    @Test
    void error_EntryContainSpace() {
        String cfgStr = """
                table t[id] (enum='s') {
                    id:int;
                    s:str;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,,
                id,s
                1,red
                2,light blue""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(EntryContainsSpace.class, valueErrs.errs().getFirst());
        EntryContainsSpace err = (EntryContainsSpace) valueErrs.errs().getFirst();
        assertEquals("t", err.table());
        assertTrue(err.source() instanceof CfgData.DCell cell &&
                cell.rowId().row() == 3 &&
                cell.col() == 1);
    }

    @Test
    void error_EntryDuplicated() {
        String cfgStr = """
                table t[id] (entry='s') {
                    id:int;
                    s:str;
                }
                """;
        Resources.addTempFileFromText("config.cfg", tempDir, cfgStr);
        String csvStr = """
                ,,,,
                id,s
                1,red
                2,red""";
        Resources.addTempFileFromText("t.csv", tempDir, csvStr);

        Context ctx = new Context(tempDir);
        CfgValueErrs valueErrs = CfgValueErrs.of();
        CfgValueParser clientValueParser = new CfgValueParser(ctx.cfgSchema(), ctx, valueErrs);
        clientValueParser.parseCfgValue();

        assertEquals(1, valueErrs.errs().size());
        assertInstanceOf(EntryDuplicated.class, valueErrs.errs().getFirst());
        EntryDuplicated err = (EntryDuplicated) valueErrs.errs().getFirst();
        assertEquals("t", err.table());
        assertTrue(err.source() instanceof CfgData.DCell cell &&
                cell.rowId().row() == 3 &&
                cell.col() == 1);
    }

}
