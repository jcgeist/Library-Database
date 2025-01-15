def generateWriteString(tableName, rows):
    writeString = "INSERT INTO `{tableName}` VALUES {rows}".format(tableName = tableName, rows = rows)
    return writeString

def writeToFile(fileName, writeString):
    openString = "./{fileName}.dump".format(fileName = fileName)
    writeFile = open(openString, "w")
    writeFile.write(writeString)
    writeFile.close()

def correctRows(rows):
    return rows[:-2] + ";"

def formatDate(date):
    splitDate = date.split("/")
    return splitDate[2] + "-" + splitDate[0] + "-" + splitDate[1]

def authorRows(authorRaw):
    rows = ""
    for line in  authorRaw:
        splitLine = line.split(",")
        id = splitLine[0]
        name = splitLine[1].strip().split(' ')
        first = name[0]
        last = name[1]
        rows += "({id},'{first}','{last}'),\n".format(id = id, first = first, last = last)
    rows = correctRows(rows)
    return rows

def generateAuthor(authorRaw):
    rows = authorRows(authorRaw)
    writeString = generateWriteString("author", rows)
    writeToFile("author", writeString)

def publisherRows(publisherRaw):
    rows = ""
    for line in publisherRaw:
        splitLine = line.split(',')
        id = splitLine[0]
        name = splitLine[1].strip()
        rows += "({id},'{name}'),\n".format(id = id, name = name)
    rows = correctRows(rows)
    return rows

def generatePublisher(publisherRaw):
    rows = publisherRows(publisherRaw)
    writeString = generateWriteString("publisher", rows)
    writeToFile("publisher", writeString)

def bookRows(bookRaw, isbnList):
    rows = ""
    for count, line in enumerate(bookRaw, start=0):
        if count % 2 == 1: 
            continue
        splitLine = line.split(",")
        isbn = splitLine[0]
        if isbn in isbnList:
            continue
        isbnList.append(isbn)
        title = splitLine[4].strip()
        year = splitLine[6][-5:-1]
        rows += "('{isbn}','{title}',{year}),\n".format(isbn = isbn, title = title, year = year)
    rows = correctRows(rows)
    return rows, isbnList

def generateBook(mainBookRaw, SouthBookRaw):
    rows, isbnList = bookRows(mainBookRaw, [])
    rows2, _ = bookRows(SouthBookRaw, isbnList)
    writeString = generateWriteString("book", rows[:-1]+",\n" + rows2)
    writeToFile("book", writeString)

def memberRows(membersRaw):
    rows = ""
    for line in membersRaw:
        splitLine = line.split(',')
        if line[:3] == "   ":
            continue
        id = splitLine[0]
        gender = splitLine[2].strip()
        name = splitLine[1].strip().split(" ")
        first = name[0]
        last = name[1]
        birthdate = formatDate(splitLine[3].strip())
        rows += "({id},'{first}','{last}','{birthdate}','{gender}'),\n".format(
            id = id, first = first, last = last, birthdate = birthdate, gender = gender)
    rows = correctRows(rows)
    return rows

def generateMember(membersRaw):
    rows = memberRows(membersRaw)
    writeString = generateWriteString("member", rows)
    writeToFile("member", writeString)

def phoneRows(authorRaw, publisherRaw):
    phoneList = []
    rows = getNumbers(authorRaw, phoneList)
    rows += getNumbers(publisherRaw, phoneList)
    rows = correctRows(rows)
    return rows

def generatePhone(authorRaw, publisherRaw):
    rows = phoneRows(authorRaw, publisherRaw)
    writeString = generateWriteString("phone", rows)
    writeToFile("phone", writeString)

def getNumbers(raw, phoneList):
    rows = ""
    for line in raw:
        splitLine = line.split(",")
        for i in range(2, len(splitLine)):
            if splitLine[i].strip() == "None":
                break
            splitNumber = splitLine[i].strip().split(" ")
            number = splitNumber[0]
            if number in phoneList:
                continue
            phoneList.append(number)
            numberType = splitNumber[1][1:2]
            rows += "('{number}','{numberType}'),\n".format(number = number, numberType = numberType)
    return rows

def author_pnumRows(authorRaw):
    rows = ""
    for line in authorRaw:
        splitLine = line.split(",")
        id = splitLine[0]
        for i in range(2, len(splitLine)):
            if splitLine[i].strip() == "None":
                break
            number = splitLine[i].strip().split(" ")[0]
            rows += "({id},'{number}'),\n".format(id = id, number = number)
    rows = correctRows(rows)
    return rows

def generateAuth_pnum(authorRaw):
    rows = author_pnumRows(authorRaw)
    writeString = generateWriteString('author_pnum', rows)
    writeToFile("author_pnum", writeString)

def borrowed_byRows(membersRaw):
    rows = ""
    id = 0
    for line in membersRaw:
        splitLine = line.split(",")
        if splitLine[0][:3] == "   ":
            isbn = splitLine[0].strip()
            out = formatDate(splitLine[1].strip())
            if len(splitLine) == 3:
                inDate = formatDate(splitLine[2].strip())
                rows += "('{isbn}',{id},'Main','{out}','{inDate}'),\n".format(isbn = isbn, id = id, out = out, inDate = inDate)
            else: rows += "('{isbn}',{id},'Main','{out}',NULL),\n".format(isbn = isbn, id = id, out = out, inDate = inDate)
        else: 
            id = splitLine[0]
    rows = correctRows(rows)
    return rows

def generateBorrowed_by(membersRaw):
    rows = borrowed_byRows(membersRaw)
    writeString = generateWriteString('borrowed_by', rows)
    writeToFile("borrowed_by", writeString)

def generatePublisher_pnum(publisherRaw):
    rows = author_pnumRows(publisherRaw)
    writeString = generateWriteString('publisher_pnum', rows)
    writeToFile("publisher_pnum", writeString)
            
def published_byRows(bookRaw, isbnList):
    rows = ""
    for count, line in enumerate(bookRaw, start=0):
        if count % 2  == 1: continue
        splitLine = line.split(",")
        isbn = splitLine[0]
        if isbn in isbnList:
            continue
        isbnList.append(isbn)
        publisher = splitLine[5].strip()
        rows += "('{isbn}',{publisher}),\n".format(isbn = isbn, publisher = publisher)
    rows = correctRows(rows)
    return rows, isbnList

def generatePublished_by(MainBookRaw, SouthBookRaw):
    rows, isbnList = published_byRows(MainBookRaw, [])
    rows2, _ = published_byRows(SouthBookRaw, isbnList)
    writeString = generateWriteString('published_by', rows[:-1] + ",\n" + rows2)
    writeToFile("published_by", writeString)

def written_byRows(bookRaw, isbnList):
    rows = ""
    isbn = 0
    skip = False
    for count, line in enumerate(bookRaw, start=0):
        splitLine = line.split(",")
        if count % 2 == 0:
            isbn = splitLine[0]
            if isbn in isbnList:
                skip = True
                continue
            isbnList.append(isbn)
        else:
            if skip:
                skip = False 
                continue
            for author in splitLine:
                rows+= "('{isbn}',{author}),\n".format(isbn = isbn, author = author.strip())
    rows = correctRows(rows)
    return rows, isbnList

def generateWritten_by(mainBookRaw, southBookRaw):
    rows, isbnList = written_byRows(mainBookRaw, [])
    rows2, _ = written_byRows(southBookRaw, isbnList)
    writeString = generateWriteString('written_by', rows[:-1] + ",\n" + rows2)
    writeToFile("written_by", writeString)

def located_atRows(bookRaw, libraryName):
    rows = ""
    for count, line in enumerate(bookRaw, start=0):
        if count % 2 != 0:
            continue
        splitLine = line.split(",")
        isbn = splitLine[0]
        numCopies = splitLine[1]
        shelf = splitLine[2]
        floor = splitLine[3]
        rows += "('{libraryName}','{isbn}',{numCopies},{availCopies},{shelf},{floor}),\n".format(
            libraryName = libraryName, isbn = isbn, numCopies = numCopies, 
            availCopies = numCopies, shelf = shelf, floor = floor
        )
    rows = correctRows(rows)
    return rows

def generateLocated_at(bookRaw, libraryName):
    rows = located_atRows(bookRaw, libraryName)
    writeString = generateWriteString("located_at", rows)
    writeToFile(libraryName, writeString)

def fileManager():
    membersRaw = open('rawData/Members.txt', "r")
    publisherRaw = open('rawData/Publisher.txt', "r")
    MainBookRaw = open('rawData/MainBook.txt', "r")
    authorRaw = open('rawData/Author.txt', "r")
    SouthBookRaw = open('rawData/SouthBook.txt', "r")
    
    generateAuthor(authorRaw)
    generatePublisher(publisherRaw)
    generateBook(MainBookRaw, SouthBookRaw)
    generateMember(membersRaw)

    authorRaw = open('rawData/Author.txt', "r")
    publisherRaw = open('rawData/Publisher.txt', "r")
    generatePhone(authorRaw, publisherRaw)

    authorRaw = open('rawData/Author.txt', "r")
    membersRaw = open('rawData/Members.txt', "r")
    publisherRaw = open('rawData/Publisher.txt', "r")
    generateAuth_pnum(authorRaw)
    generateBorrowed_by(membersRaw)
    generatePublisher_pnum(publisherRaw)

    MainBookRaw = open('rawData/MainBook.txt', "r")
    SouthBookRaw = open('rawData/SouthBook.txt', "r")
    generatePublished_by(MainBookRaw, SouthBookRaw)

    MainBookRaw = open('rawData/MainBook.txt', "r")
    SouthBookRaw = open('rawData/SouthBook.txt', "r")
    generateWritten_by(MainBookRaw, SouthBookRaw)

    MainBookRaw = open('rawData/MainBook.txt', "r")
    generateLocated_at(MainBookRaw, "Main")
    
    SouthBookRaw = open('rawData/SouthBook.txt', "r")
    generateLocated_at(SouthBookRaw, "South Park")
    
    membersRaw.close()
    publisherRaw.close()
    MainBookRaw.close()
    authorRaw.close()
    SouthBookRaw.close()


fileManager()