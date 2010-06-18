-- queries

SELECT PROVENANCE 
	c.name 
FROM
	customers c;

SELECT PROVENANCE 
	count(*) AS numcust, 
	country 
FROM 
	customers c 
GROUP BY country;

SELECT *
FROM 
	(SELECT PROVENANCE 
		count(*) AS numcust, 
		country 
	FROM 
		customers c 
	GROUP BY country) AS custcount
WHERE 
	country = 'CH';

SELECT PROVENANCE 
	i.name AS item, 
	CASE WHEN sum(quantity) IS NOT NULL THEN sum(quantity) ELSE 0 END AS totalqnt 
FROM 
	items i 
	LEFT OUTER JOIN 
	orderitem oi ON (i.iid = oi.iid) 
GROUP BY 
	i.iid, i.name;

SELECT *
FROM 
	(SELECT PROVENANCE * FROM itemquantity) AS item
WHERE
	prov_public_orderitem__oid = 3;


SELECT PROVENANCE 
	i.* 
FROM 
	items i 
	JOIN 
	orderitem oi ON (i.iid = oi.iid);

SELECT PROVENANCE 
	items.name, 
	items.price 
FROM 
	(SELECT i.iid, i.name, i.price, s.name AS sname, s.quality 
		FROM items i JOIN sources s ON sourceId = sid) PROVENANCE (sname, quality) AS items 
	JOIN orderitem oi ON (items.iid = oi.iid);
 
SELECT PROVENANCE
	*
FROM
	(SELECT * FROM items WHERE price > 40
	UNION
	SELECT * FROM items WHERE price < 55) AS topandlow;

SELECT PROVENANCE
	*
FROM
	(SELECT * FROM items WHERE price > 80
	INTERSECT
	SELECT i.* FROM items i, sources s WHERE sourceId = sid AND s.name LIKE 'WWID%') AS topfromone;
	

SELECT PROVENANCE 
	(x.revenue::DECIMAL / y.revenue::DECIMAL) AS relative_topcust_revenue,
	 x.country
FROM 
	(SELECT sum(revenue) AS revenue, country 
	FROM topcust
	GROUP BY country) AS x,
	(SELECT sum(price * quantity) AS revenue, country
	FROM customers c, orders o, orderitem oi, items i
	WHERE c.cid = o.cid AND o.oid = oi.oid AND oi.iid = i.iid
	GROUP BY c.country) AS y 
WHERE 
	x.country = y.country;

SELECT PROVENANCE 
	(x.revenue::DECIMAL / y.revenue::DECIMAL) AS relative_topcust_revenue, 
	x.country
FROM 
	(SELECT sum(revenue) AS revenue, country 
	FROM topcust
	GROUP BY country) BASERELATION  AS x,
	(SELECT sum(price * quantity) AS revenue, country
	FROM customers c, orders o, orderitem oi, items i
	WHERE c.cid = o.cid AND o.oid = oi.oid AND oi.iid = i.iid
	GROUP BY c.country) BASERELATION AS y 
WHERE 
	x.country = y.country;

SELECT PROVENANCE 
	(x.revenue::DECIMAL / y.revenue::DECIMAL) AS relative_topcust_revenue, 
	x.country
FROM 
	(SELECT sum(revenue) AS revenue, country 
	FROM topcust BASERELATION
	GROUP BY country)  AS x,
	(SELECT sum(price * quantity) AS revenue, country
	FROM customers c, orders o, orderitem oi, items i
	WHERE c.cid = o.cid AND o.oid = oi.oid AND oi.iid = i.iid
	GROUP BY c.country) BASERELATION AS y 
WHERE 
	x.country = y.country;

SELECT PROVENANCE
	i.name, 
	sum(price * quantity) AS total
FROM 
	items i,
	orderitem oi
WHERE
	i.iid = oi.iid
GROUP BY 
	i.name
ORDER BY total DESC
LIMIT 3;



--- subqueries

SELECT PROVENANCE
	*
FROM
	customers c1
WHERE
	EXISTS (SELECT * FROM customers c2 WHERE c1.cid != c2.cid AND c1.country = c2.country) 
ORDER BY name;

SELECT
	count(*) AS numorders,	
	c.name
FROM 
	customers c,
	orders o
WHERE
	c.cid = o.cid
GROUP BY
	c.name
HAVING count(*) = 
	(SELECT max (numorders) 
	FROM
		(SELECT
			count(*) AS numorders
		FROM
			customers c2,
			orders o2
		WHERE
			c2.cid = o2.cid
		GROUP BY
			c2.cid) AS nums)
;

SELECT PROVENANCE
	count(*) AS numorders,	
	c.name
FROM 
	customers c,
	orders o
WHERE
	c.cid = o.cid
GROUP BY
	c.name
HAVING count(*) = 
	(SELECT max (numorders) 
	FROM
		(SELECT
			count(*) AS numorders
		FROM
			customers c2,
			orders o2
		WHERE
			c2.cid = o2.cid
		GROUP BY
			c2.cid) AS nums)
;


--- TPCH


select provenance
	cntrycode,
	count(*) as numcust,
	sum(c_acctbal) as totacctbal
from
	(
		select
			substring(c_phone from 1 for 2) as cntrycode,
			c_acctbal
		from
			customer
		where
			substring(c_phone from 1 for 2) in
				('20', '21', '16', '19', '15', '29', '10')
			and c_acctbal > (
				select
					avg(c_acctbal)
				from
					customer
				where
					c_acctbal > 0.00
					and substring(c_phone from 1 for 2) in
						('20', '21', '16', '19', '15', '29', '10')
			)
			and not exists (
				select
					*
				from
					orders
				where
					o_custkey = c_custkey
			)
	) as custsale
group by
	cntrycode
order by
	cntrycode;

