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
	cat << EOT
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

EOT
	for field in $fields
	do
		if [ $_fromFile -eq 0 ]
		then
                        type="$(guessType $o)"
			allups="$(toUpper $field )"
			alldowns="$(toLower $field )"
		else
                        type="${types[$o]}"
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
		_line="case \"$allups\":\n    ret.set${firstUp}"
		case "${type}" in
			int)
				_line="${_line}(rs.getInt(field));"
				;;
			LocalDate)
				_line="${_line}(rs.getDate(field) == null ? null : rs.getDate(field).toLocalDate());"
				;;
			boolean | Boolean)
				_line="${_line}(\"T\".equals(rs.getString(field)));"
				;;
			String)
				_line="${_line}(rs.getString(field));"
				;;
			*)
				_line="${_line}(rs.get${type}(field));"
				;;
		esac
		echo -ne "${_line}\n    break;\n"
		let o++
	done
	cat << EOT
                default:
                    logger.error("Unhandled field in " + dbName + " found: " + meta.getColumnLabel(o) + " at pos: " + o);
                };
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("Couldn't retrieve dataset in "  + ${className}Dto.class.getName());
            logger.error("Error: " + e.getLocalizedMessage());
        }
        
        return ret;
        }
EOT
}

function saveToDB() {
	local o=0
	cat << EOT
	
	/**
     * Takes an SQL-Statement as String and executes it<BR/> (<B>this is NOT! a query!</B>).<BR/>
     * SQL statements can be e.g. <BR/>"update verordn set termine='' where rez_nr='ER1'"<BR/>
     * In theory even <BR/>"alter table ..."<BR/> should be possible...<BR/>
     * @param sql - the SQL statement as String
     */
    private void updateDataset(String sql) {
        Connection conn;
        try {
            conn = new DatenquellenFactory(ik.digitString()).createConnection();
            boolean rs = conn.createStatement().execute(sql);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error("In updateDataset:");
            logger.error(e.getLocalizedMessage());
            logger.error("SQL-Statement was: '" + sql + "'");
        }
    }
	
    public boolean saveToDB($className dataset) {
    	// FIXME: set appropriate getter to match mainIdentifier
        String sql="select id from " + dbName + " where " + mainIdentifier + "='" + dataset.get${className}Nr() + "'";
        boolean isNew = false;
        
        try (Connection conn = new DatenquellenFactory(ik.digitString())
                .createConnection()) {
            // FIXME: set appropriate getter to match mainIdentifier
            if ( dataset.get${className}Nr() != null && !dataset.get${className}Nr().isEmpty()) {
            
                ResultSet rs = conn.createStatement()
                        .executeQuery(sql);
                if (rs.next()) {
                    isNew = false;
                    // FIXME: set appropriate getter to match mainIdentifier
                    logger.debug("${className} will " + dataset.get${className}Nr() + " be updated");
                } else {
                    isNew = true;
                    // FIXME: set appropriate getter to match mainIdentifier
                    logger.debug("${className} will " + dataset.get${className}Nr() + " be added.");
                }
            } else {
                logger.error("Given " + mainIdentifier + " was empty or Null - this shouldn't happen - get " + mainIdentifier + " before saving it");
                return false;
            }
            if (isNew) {
                sql="insert into " + dbName + " ";
            } else {
                sql="update " + dbName + " ";
            }
            sql = sql.concat(createFullDataset(dataset));
            if (!isNew)
            	// FIXME: set appropriate getter to match mainIdentifier
                sql = sql.concat(" WHERE " + mainIdentifier + "='" + dataset.get${className}Nr() + "' LIMIT 1");
            updateDataset(sql);
        } catch (SQLException e) {
        	// FIXME: set appropriate getter to match mainIdentifier
            logger.error("Could not save ${className} " + dataset.get${className}Nr() + " to Database", e);
            return false;
        }
        return true;

    }
        
    private String createFullDataset(${className} dataset) {
    	String sql = "set "
EOT
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
		
		# echo -ne "+ \"${allups}='\" + dataset.get${firstUp}() + \"'"
		_line="+ \"${allups}="
		case "${types[$o]}" in
			int)
				_line="${_line}\" + dataset.get${firstUp}() + \""
				;;
			String)
				_line="${_line}\" + quoteNonNull(dataset.get${firstUp}()) + \""
				;;
			LocalDate)
				_line="${_line}\" + quoteNonNull(dataset.get${firstUp}()) + \""
				;;
			boolean)
			# Two versions avail:
			# The latter will use only the default getter/setter created via Eclipse
			# The first will use the getBOOLMEMBER() that returns the bool as "T"/"F" String
			#  (created by this script)
				_line="${_line}\" + quoteNonNull(dataset.get${firstUp}()) + \""
#				_line="${_line}\" + (dataset.is${firstUp}() ? \"'T'\" : \"'F'\") + \""
				;;
			*)
				_line="${_line}'\" + dataset.get${firstUp}() + \"'"
				;;
		esac
		echo -ne ${_line}
		[ $o -lt $(( ${#types[@]} - 1 )) ] && echo -ne ","
		echo -ne "\"\n"
		let o++
	done
	echo ";"
	cat << EOT
            return sql;
        }
    }
    
    private String quoteNonNull(Object val) {
        return (val == null ? "NULL" : "'" + val + "'");
    }
EOT
}

function varsInConstructor() {
	local o=0
	for field in $fields
	do
		echo "$( guessType $o) $( toLower $field );"
		let o++
	done
}

function extraBools() {
	local o=0
	for field in ${fields}
	do
		[ $DEBUG -gt 1 ] && echo "DEBUG: check type: ${types[$o]} with o=$o"
		if [ "A${types[$o]}" != "Aboolean" ]
		then
			let o++
			continue
		fi
		_upper="$( toUpper ${field:0:1})${field:1}"
		cat << EOT
	public String get${_upper}() {
		return (is${_upper}() ? "T" : "F" ); 
	}	
EOT
		let o++
	done

}

function dtoHeader() {

	cat << EOT
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mandant.IK;
import sql.DatenquellenFactory;
	
public class ${className}Dto {
    private static final Logger logger = LoggerFactory.getLogger(${className}Dto.class);
    
    private static final String dbName="${table}";
    // FIXME: set the following value to something like "REZ_NR" or whatever makes the most sense...
    private static final String mainIdentifier="${table}_NR";
    private IK ik;
    
    public ${className}Dto(IK Ik) {
        ik = Ik;
    }

}
  
EOT

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
echo "Paste the following as Dto-class:"
echo "----------SNIP----------"
echo ""
dtoHeader
echo ""
echo "----------SNIP----------"
echo ""
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
echo "And paste the following as extra getter/setter in class: "
echo "----------SNIP----------"
echo ""
extraBools
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
