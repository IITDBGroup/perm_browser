/* demo database */
SET datestyle TO US;

DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS orderitem CASCADE;
DROP TABLE IF EXISTS items CASCADE ;
DROP TABLE IF EXISTS sources CASCADE;

DROP VIEW IF EXISTS topccust;
DROP VIEW IF EXISTS newcust;
DROP VIEW IF EXISTS itemquantity;

CREATE TABLE customers (
	cid int8 PRIMARY KEY,
	name text,
	country text
);

CREATE TABLE sources (
	sid int8 PRIMARY KEY,
	name text,
	contact_person text,
	quality decimal
);

CREATE TABLE items (
	iid int8 PRIMARY KEY,
	name text,
	price int,
	sourceId int8 REFERENCES sources (sid)
);

CREATE TABLE orders (
	oid int8 PRIMARY KEY,
	cid int8 REFERENCES customers (cId),
	odate date
);



CREATE TABLE orderitem (
	oid int8 REFERENCES orders (oId),
	iid int8 REFERENCES items (iid),
	quantity int,
	CONSTRAINT pk PRIMARY KEY (oid, iid)
);



CREATE VIEW topcust AS 
SELECT c.cid, sum(price * quantity) AS revenue, country
FROM customers c, orders o, orderitem oi, items i
WHERE c.cid = o.cid AND o.oid = oi.oid AND oi.iid = i.iid
GROUP BY c.cid, country
HAVING sum(price * quantity) > 100;

CREATE VIEW newcust AS
SELECT * 
FROM customers c
WHERE NOT EXISTS 
	(SELECT * FROM orders o 
	WHERE o.cid = c.cid);

CREATE VIEW itemquantity AS
SELECT 
	i.name AS item, 
	CASE WHEN sum(quantity) IS NOT NULL THEN sum(quantity) ELSE 0 END AS totalqnt 
FROM 
	items i LEFT OUTER JOIN orderitem oi ON (i.iid = oi.iid) 
GROUP BY 
	i.iid, i.name;

-- data

INSERT INTO customers VALUES (1,'Remo Welti','CH');
INSERT INTO customers VALUES (2,'Beat Ziegli','CH');
INSERT INTO customers VALUES (3,'Anne Meier','DE');
INSERT INTO customers VALUES (4,'Herbert Heinzel','DE');
INSERT INTO customers VALUES (5,'Albert Miller','US');
INSERT INTO customers VALUES (6,'June Fisher','US');
INSERT INTO customers VALUES (7,'Lord Shetterton','GB');
INSERT INTO customers VALUES (8,'Mr Henrick Blob','GB');
INSERT INTO customers VALUES (9,'Heiner Hummer', 'GB');

INSERT INTO sources VALUES (1, 'WWID - World Wide Item Dictionary', 'Peter Petersen', 0.3);
INSERT INTO sources VALUES (2, 'SIIS - Swiss institute of item specifications', 'Beat Blunschli', 0.9);
INSERT INTO sources VALUES (3, 'IPA - Item Price Agency', 'Arun Avashi', 0.6);

INSERT INTO items VALUES (1, 'lawnmower', 130, 1);
INSERT INTO items VALUES (2, 'hedge trimmer', 50, 1);
INSERT INTO items VALUES (3, 'hedge trimmer deluxe', 120, 3);
INSERT INTO items VALUES (4, 'shovel', 10, 2);
INSERT INTO items VALUES (5, 'pickax', 15 , 1);
INSERT INTO items VALUES (6, 'tulip seeds', 2, 3);
INSERT INTO items VALUES (7, 'fir cones', 5, 1);
INSERT INTO items VALUES (8, 'liquid fertilizer', 7, 1);
INSERT INTO items VALUES (9, 'mineral fertilizer', 5, 2);
INSERT INTO items VALUES (10, 'organic fertilizer', 12, 3);

INSERT INTO orders VALUES (1, 1, '01-01-1999');
INSERT INTO orders VALUES (2, 1, '10-02-2002');
INSERT INTO orders VALUES (3, 6 , '03-05-2000');
INSERT INTO orders VALUES (4, 7 , '11-11-2001');
INSERT INTO orders VALUES (5, 4 , '06-17-2005');
INSERT INTO orders VALUES (6, 2 , '07-07-2003');
INSERT INTO orders VALUES (7, 9 , '01-01-2004');

INSERT INTO orderitem VALUES (1,1,1);
INSERT INTO orderitem VALUES (1,6,20);
INSERT INTO orderitem VALUES (1,10,3);
INSERT INTO orderitem VALUES (2,4,1);
INSERT INTO orderitem VALUES (3,7,4);
INSERT INTO orderitem VALUES (3,4,2);
INSERT INTO orderitem VALUES (4,3,1);
INSERT INTO orderitem VALUES (5,2,1);
INSERT INTO orderitem VALUES (5,4,1);
INSERT INTO orderitem VALUES (6,9,3);
INSERT INTO orderitem VALUES (7,4,15);


