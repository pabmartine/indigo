CREATE TABLE IF NOT EXISTS users ( 
					 id INTEGER PRIMARY KEY AUTOINCREMENT,
                     username TEXT NOT NULL,
                     password TEXT NOT NULL,
                     kindle TEXT,
                     role TEXT NOT NULL,
                     language TEXT NOT NULL,
                     show_random_books INTEGER DEFAULT 1,
                     UNIQUE(username)
                   );                 
                   
CREATE TABLE IF NOT EXISTS authors ( 
					 id INTEGER PRIMARY KEY,
                     name TEXT NOT NULL,
                     sort TEXT NOT NULL,
                     description TEXT NOT NULL,
                     image TEXT,
                     provider TEXT,
                     UNIQUE(name)
                   );             
                   
CREATE TABLE IF NOT EXISTS configurations ( 
					 key TEXT PRIMARY KEY,
                     value TEXT NOT NULL
                   );              
                   
CREATE TABLE IF NOT EXISTS tags ( 
					 id INTEGER PRIMARY KEY,
                     image TEXT NOT NULL
                   );                     
                   
CREATE TABLE IF NOT EXISTS books ( 
					 id INTEGER PRIMARY KEY,
                     rating INTEGER,
                     similar TEXT,
					 provider TEXT
                   );

CREATE TABLE IF NOT EXISTS favorite_books (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					book INTEGER,
					user INTEGER,
					UNIQUE(book, user)
);         

CREATE TABLE IF NOT EXISTS favorite_authors (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					author INTEGER,
					user INTEGER,
					UNIQUE(author, user)
);

CREATE TABLE IF NOT EXISTS notifications (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					type TEXT,
					book INTEGER,
					user INTEGER,
					status TEXT,
					read_by_user INTEGER,
					read_by_admin INTEGER
					error TEXT
);  