#!/bin/bash
DEBUG=0

##########
#
# This was orginally written on a Mac & since the Mac (still) uses Bash <4.0 some of the nicer things are not avail - most noteably toUpper etc all
#   also the mysql-client & socket may not be where you expect them on a Linux machine
#
# Howto create a nice empty klasse + klasseDto from a table:
#
# - Check the MySQL params below - Socket, user, password and DBName
# - run skript it will create 3 section - 2 for the dto-class 1 for actual class
# - paste them to the according files
# - it will be quite ugly, since DBField-Names were taken directly if you want a nicer one, do the following
# - Only paste the class-member section to the class.java file
# - edit the names to your likings w/o changing the order & don't insert any indention
# - copy your version of the member-defs to a text-file and re-run this script with the param -ff TEXTFILEYOUCREATED
# - copy & paste the resulting 2 sections to the Dto-class
#
# - have Eclipse create all getter/setter from the members you pasted above
# - check the Dto-class reads the correct data-type from DB (rs.getString vs rs.getInt etc)
# - re-check the dataTypes for consistancy

_fromFile=0
_file=""

while [ ${#@} -gt 0 ]
do
	case "$1" in
		-ff)
			_fromFile=1
			_file="$2"
			shift 2
			;;
		*)
			echo "Unknown Parameter \"$1\" passed in"
			exit 1
			;;
	esac
done
[ "A$passw" == "A" ] && read -sp "Password: " passw && echo
[ "A$dbName" == "A" ] && read -p "Database name: " dbName && echo
[ "A$table" == "A" ] && read -p "Tablename: " table && echo

# Defaults (Maria-DB on Mac):
socket="/usr/local/mariadb/server/data/mysqld.sock"
user="lolibert"

mysqlcmd="mysql -u $user -S $socket -p$passw $dbName -N"
# _file="$1"

function trim() {
	echo $@
}

function toUpper() {
	echo "${1}" |tr '[:lower:]' '[:upper:]'
}

function toLower() {
	echo "${1}" |tr '[:upper:]' '[:lower:]'
}

function guessType() {
	idx=$1
	type="${types[$idx]}"
        if [[ $type =~ "char" ]] || [[ $type =~ "text" ]]
        then
                echo -ne "String"
        elif [[ $type =~ "enum('T'," ]]
        then
                echo -ne "boolean"
        elif [[ $type =~ "date" ]] || [[ $type =~ "LocalDate" ]]
        then
                echo -ne "LocalDate"
        elif [[ $type =~ "int" ]]
        then
                echo -ne "int"
        else
                echo -ne "String"
        fi
        [ $DEBUG -gt 1 ] && echo "DEBUG type=\"$type\""

}

function setFieldsInClass() {
	local o=0
	cat << -EOT
    private $className ofResultset(ResultSet rs) {
        $className ret = new $className();
        
        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            logger.error("Could not retrieve metaData", e);
            return null;
        }
        try {
            for(int o=1;o<=meta.getColumnCount();o++) {
                String field = meta.getColumnLabel(o).toUpperCase();
                // logger.debug("Checking: " + field + " in " + o);
                switch (field) {

-EOT
	for field in $fields
	do
		type="$(guessType $o)"
		if [ $_fromFile -eq 0 ]
		then
			allups="$(toUpper $field )"
			alldowns="$(toLower $field )"
		else
			allups="$( toUpper ${fieldsDB[$o]} )"
			alldowns="$field"
		fi
		if [[ "${field:1:1}" =~ [[:lower:]] ]]
		then
			firstUp="${allups:0:1}${alldowns:1}"
		else
			firstUp="${field}"
		fi
		[ $DEBUG -gt 1 ] && echo "DEBUG: allups=\"$allups\""
		[ $DEBUG -gt 1 ] && echo "DEBUG: firstUp=\"$firstUp\""
		[ $DEBUG -gt 1 ] && echo "DEBUG: type=\"${type}\""
		echo -ne "case \"$allups\":\n    ret.set${firstUp}(rs.get${type}(field));\n    break;\n"
		let o++
	done
	cat << -EOT
                default:
                    logger.error("Unhandled field in $table found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in $className");
            logger.error("Error: " + e.getLocalizedMessage());
        }
        
        return ret;
        }
-EOT
}

function saveToDB() {
	local o=0
	cat << -EOT
    public void saveToDB($className dataset) {
        String sql = "insert into " + dbName + " set "
-EOT
	for field in $fields
	do
		if [ $_fromFile -eq 0 ]
        then
            allups="$(toUpper $field )"
            alldowns="$(toLower $field )"
			firstUp="${allups:0:1}${alldowns:1}"
        else
        	allups="$( toUpper ${fieldsDB[$o]} )"
        	if [[ "${field:1:1}" =~ [[:upper:]] ]]
        	then
        		firstUp="$field"
        	else
                firstUp="$( toUpper ${field:0:1} )${field:1}"
            fi
        fi
		
		echo -ne "+ \"${allups}='\" + dataset.get${firstUp}() + \"'"
		[ $o -lt $(( ${#types[@]} - 1 )) ] && echo -ne ","
		echo -ne "\"\n"
		let o++
	done
	echo ";"
	cat << -EOT
        try {
            Connection conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            logger.error("Could not save dataset " + dataset.toString( ) + " to Database, table $table", e);
        }
    }
-EOT
}

function varsInConstructor() {
	o=0
	for field in $fields
	do
		echo "$( guessType $o) $( toLower $field );"
		let o++
	done
}

if [ $_fromFile -eq 0 ]
then
	types=($( $mysqlcmd -t -e "show fields from $table"|cut -d'|' -f3| grep -v '+----' ))
	fields="$( $mysqlcmd -t -e "show fields from $table" |cut -d'|' -f2 | grep -v '+----' )"
	[ $DEBUG -gt 0 ] && echo "DEBUG: fields: \"$fields\""
else
	types=( $( function first() { echo $1; }; cat $_file| while read line;do echo $( first $line );done ) )
	fields="$( function scnd() { echo "${2/;/}" ; }; cat $_file| while read line;do echo $( scnd $line );done )"
	fieldsDB=($( $mysqlcmd -t -e "show fields from $table" |cut -d'|' -f2 | grep -v '+----' ))
	[ $DEBUG -gt 1 ] && echo "Types: ${types[@]} and 1st: ${types[1]}" 
	[ $DEBUG -gt 0 ] && echo "Fields: \"$fields\""
	[ $DEBUG -gt 0 ] && echo "DB-Fields: \"${fieldsDB[@]}\""
fi

className="$( echo $( toUpper ${table:0:1} )${table:1})"
echo "Paste the following as \"ofResultSet\" method in Dto-class:"
echo "----------SNIP----------"
echo ""
setFieldsInClass
echo ""
echo "----------SNIP----------"
echo ""
echo "And paste the following as saveToDB method in Dto-class: "
echo "----------SNIP----------"
echo ""
saveToDB
echo ""
echo "----------SNIP----------"
echo ""
if [ $_fromFile -eq 0 ]
then
	echo "And paste the following as members of class:"
	echo "----------SNIP----------"
	echo ""
	varsInConstructor
fi
