# cfgMgr.go 的设计
每个table文件里，怎么提供访问接口。
先提供唯一名字的mgr，由mgr来提供访问。
否则容易重名。

# 主键，副键，联合形态

最常见的主键，一般只用一个filed来定义。

键支持联合形态，用多个field构成一个唯一索引。
可以把主键设置成联合形态。
请参考other.lootitem。

主键并不是唯一的索引方式，支持增加副键。并且副键也支持联合形态。
请参考other.lootitem。

如果主键是联合形态，不能被ref，只能=>到其中的一个field，会返回所有符合这个key的容器。

主键可以直接=>到联合键。

GO版本不支持直接ref到一个struct类型的主键