CREATE OR REPLACE FUNCTION check_unique() RETURNS trigger AS $BODY$
				BEGIN IF (EXISTS (SELECT * FROM types WHERE plural_name = NEW.type_name)) THEN 
				RAISE EXCEPTION 'Transaction aborted. Details -> type_name duplicates existing plural_name.'; END IF; 
				IF (EXISTS (SELECT * FROM types WHERE type_name = NEW.plural_name)) THEN 
				RAISE EXCEPTION 'Transaction aborted. Details -> plural_name duplicates existing type_name.'; END IF; RETURN NEW;
				END$BODY$  LANGUAGE plpgsql VOLATILE COST 100;
-----
		CREATE TRIGGER "check_unique TR" BEFORE INSERT OR UPDATE OF type_name, plural_name ON types FOR EACH ROW EXECUTE PROCEDURE check_unique()
-----
CREATE OR REPLACE FUNCTION create_composition(container varchar[], contained varchar[], component varchar[], is_plu boolean[], is_opt boolean[])
  RETURNS integer[] AS
$BODY$DECLARE
	result integer[]; --vettore che contiene le voci che non vengono inserite
	iter integer;
BEGIN
iter :=1;
FOR i IN array_lower(container,1) .. array_upper(container,1) LOOP
	
	--verifica corretezza della composizione
	--prima query se presente a parti invertite container/contained e contained/container, controlla se esiste già la relazione o se il component_name è vuoto 
	--seconda query verifica se già presente
	IF EXISTS (SELECT * FROM type_composition WHERE container_type = contained[i]
		AND contained_type = container[i]) OR EXISTS
		(SELECT * FROM type_composition WHERE container_type = container[i]
		AND component_name = component[i]) OR component[i] = ''
		THEN
			--in caso di errore viene inserito nel vettore
			result[iter]=i;
			iter = iter+1;
		ELSE
			--inserimento della regola di composizione
			INSERT INTO type_composition(container_type, contained_type, component_name, optional, is_plural)
			VALUES(container[i], contained[i], component[i], is_opt[i], is_plu[i]);
		END IF;
END LOOP;	

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
 COMMENT ON FUNCTION create_composition(varchar[], varchar[], varchar[], boolean[], boolean[]) IS 'Funzione che riceve regole di composizione e se sono corrette le crea';
-----
CREATE OR REPLACE FUNCTION create_object_composition(texobj_container integer, texobj_container_type varchar, tex_start integer, tex_end integer)
  RETURNS boolean AS
$BODY$DECLARE
	existing_contained_type RECORD;
	existing_component RECORD;
	composition objects_composition;
	result boolean;
	i integer;
BEGIN
	result := 0;
	--primo loop sul risultato della ricerca dei tipi contenuti 
	FOR existing_contained_type IN SELECT * FROM type_composition 
	WHERE container_type = texobj_container_type LOOP
		
		--contatore di posizione
		i = 1;

 		--secondo loop sugli oggetti contenuti
		FOR existing_component IN SELECT * FROM textual_objects
		WHERE start >= tex_start AND start < tex_end
			AND type_name = existing_contained_type.contained_type
			AND is_plural = existing_contained_type.is_plural
			ORDER BY "start" LOOP

			--inserimento della composizione
			INSERT INTO objects_composition(id_container, id_contained, pos, ty_of_relation)
			VALUES(texobj_container, existing_component.id_tex_obj,i,false);
			--aggiornamento della posizione
			i = i+1;
			result := 1;
		END LOOP;

	END LOOP;

RETURN result;
END;


$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_object_composition(integer, varchar, integer, integer) IS 'Funzione che riceve tutti i dati relativi a un textual object, e se è possibile crea la composizione con gli oggetti da lui contenuti';
-----
CREATE OR REPLACE FUNCTION create_or_return_att_for_admin(att_type integer, att_text text, att_obj integer)
  RETURNS attribute_values AS
$BODY$DECLARE
	existing_att attribute_values;
	nowtimestamp timestamp without time zone;
	pk integer;

BEGIN
	-- verifica se l'attributo esiste
	SELECT INTO existing_att *
	FROM attribute_values
	WHERE id_att_type = att_type AND
	id_tex_obj = att_obj AND 
	id_user=0;

	-- se la lista è nulla viene inserita
	IF existing_att IS NULL THEN

		-- selezione di un timestamp aggiornato
		SELECT INTO nowtimestamp LOCALTIMESTAMP;

		-- inserimento dell'attributo
		INSERT INTO attribute_values(content, timestamp, id_user, id_tex_obj, id_att_type)
			VALUES(att_text, nowtimestamp, 0, att_obj, att_type) RETURNING id_att_value INTO pk;

		-- ricerca attributo inserito
		SELECT INTO existing_att *
		FROM attribute_values
		WHERE id_att_value = pk;
	ELSE
		RETURN existing_att;
		
	END IF;
	
RETURN existing_att;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_att_for_admin(integer, text, integer) IS 'Funzione che riceve i dati relativi a un attributo e se possibile lo crea. Solo per amministratori';
-----
CREATE OR REPLACE FUNCTION create_or_return_att_for_user(att_type integer, att_text text, att_user integer, att_obj integer)
  RETURNS attribute_values AS
$BODY$DECLARE
	existing_att attribute_values;
	nowtimestamp timestamp without time zone;
	pk integer;

BEGIN
	-- verifica se l'attributo esiste
	SELECT INTO existing_att *
	FROM attribute_values
	WHERE id_att_type = att_type AND
	id_user = att_user AND
	id_tex_obj = att_obj;

	-- se la lista è߽ nulla viene inserita
	IF existing_att IS NULL THEN

		-- selezione di un timestamp aggiornato
		SELECT INTO nowtimestamp LOCALTIMESTAMP;

		-- inserimento dell'attributo
		INSERT INTO attribute_values(content, timestamp, id_user, id_tex_obj, id_att_type)
			VALUES(att_text, nowtimestamp, att_user, att_obj, att_type)RETURNING id_att_value INTO pk;

		-- ricerca attributo inserito
		SELECT INTO existing_att *
		FROM attribute_values
		WHERE id_att_value = pk;
	ELSE
		RETURN existing_att;

	END IF;
	
RETURN existing_att;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_att_for_user(integer, text, integer, integer) IS 'Funzione che riceve i dati relativi a un attributo e nel rispetto delle regole di associazione degli attributi lo crea. Solo per utenti';
-----
CREATE OR REPLACE FUNCTION create_or_return_obj(tex_type varchar, is_plu boolean, tex_start integer, tex_occ_id integer)
  RETURNS textual_objects AS
$BODY$DECLARE
	existing_texobj textual_objects;
	pk integer;
BEGIN
	-- verifica dell'esistenza del textual object ricevuto in input
	SELECT INTO existing_texobj *
	FROM textual_objects
	WHERE start = tex_start AND  
		type_name = tex_type AND
		is_plural = is_plu;

	-- se la lista dei risultati è vuota
	IF existing_texobj IS NULL THEN
	
		-- inserimento dell'oggetto
		INSERT INTO textual_objects(type_name, is_plural, start)
			VALUES(tex_type, is_plu, tex_start) RETURNING id_tex_obj INTO pk;
				
			SELECT INTO existing_texobj *
			FROM textual_objects
			WHERE id_tex_obj = pk;

		-- inserimento dell'elemento
		INSERT INTO elements(id_tex_obj, id_text_occurence)
			VALUES(existing_texobj.id_tex_obj, tex_occ_id);
									
	END IF;

RETURN existing_texobj;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_obj(varchar, boolean, integer, integer) IS 'Funzione che riceve i dati relativi a un textual object, se è gia presente nel db lo restituisce altrimenti lo crea';
-----
CREATE OR REPLACE FUNCTION create_or_return_rto(component integer[], obj_type varchar, is_plu boolean, obj_label text)
  RETURNS textual_objects AS
$BODY$DECLARE
	existing_texobj textual_objects;
	first_start integer;
	pk integer;
BEGIN
	--ricerca start primo component
	SELECT INTO first_start start
	FROM elements AS el, text_occurence AS tex
	WHERE el.id_tex_obj = component[1] AND 
		el.id_text_occurence = tex.id_text_occurence;
		
	--verifica esistenza del textual object
	SELECT INTO existing_texobj *
	FROM textual_objects
	WHERE start = first_start AND  
		type_name = obj_type AND
		label = obj_label AND
		is_plural = is_plu;
		
	-- se la lista dei risultati è vuota
	IF existing_texobj IS NULL THEN
	
		-- inserimento dell'oggetto
		INSERT INTO textual_objects(type_name, is_plural, start, label)
			VALUES(obj_type, is_plu, first_start, obj_label)RETURNING id_tex_obj INTO pk;
				
			SELECT INTO existing_texobj *
			FROM textual_objects
			WHERE id_tex_obj=pk;
				
		-- inserimento delle componenti	
		FOR i IN array_lower(component,1) .. array_upper(component,1) LOOP
			INSERT INTO objects_composition(id_container, id_contained, pos, ty_of_relation)
				VALUES(existing_texobj.id_tex_obj, component[i],i,true);
		END LOOP;			

	END IF;			

RETURN existing_texobj;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_rto(integer[], varchar, boolean, text) IS 'Crea e restituisce un repeated textual object';
-----
CREATE OR REPLACE FUNCTION create_or_return_sto(new_text text, tex_len integer, tex_start integer, tex_end integer, tex_type varchar, is_plu boolean)
  RETURNS textual_objects AS
$BODY$DECLARE
	existing_text text_occurence;
	existing_texobj textual_objects;
	flag boolean;
BEGIN
	--inserimento del testo
	existing_text := create_or_return_text(new_text, tex_len, tex_start, tex_end);

	--inserimento textual object
	existing_texobj := create_or_return_obj(tex_type, is_plu, tex_start, existing_text.id_text_occurence);

	--composizione degli oggetti
	flag := create_object_composition(existing_texobj.id_tex_obj, tex_type, tex_start, tex_end);

RETURN existing_texobj;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_sto(text, integer, integer, integer, varchar, boolean) IS 'Funzione che riceve tutti i dati relativi a un single textual object, se � gia presente nel db lo restituisce altrimenti lo crea. Il tutto viene realizzato tramite la chiamata delle funzioni:
create_or_return_text
create_or_return_obj
create_object_composition';
-----
CREATE OR REPLACE FUNCTION create_or_return_text(new_tex text, tex_len integer, tex_start integer, tex_end integer)
  RETURNS text_occurence AS
$BODY$DECLARE
	existing_text texts;
	existing_occ text_occurence;
	pk integer;

BEGIN
	-- verifica dell'esistenza del text ricevuto in input
	SELECT INTO existing_text *
	FROM texts
	WHERE lenght = tex_len AND  
		cached_text = new_tex;

	-- se la lista dei risultati è vuota
	IF existing_text IS NULL THEN
	
		-- inserimento del text
		INSERT INTO texts(cached_text, lenght)
			VALUES(new_tex, tex_len) RETURNING id_text INTO pk;
				
			-- inserimento dell'occurence
			INSERT INTO text_occurence("start", "end", id_text)
				VALUES(tex_start, tex_end, pk) RETURNING id_text_occurence INTO pk;

			SELECT INTO existing_occ *
			FROM text_occurence AS occ
			WHERE occ.id_text_occurence = pk;
	ELSE	
		-- verifica dell'esistenza dell'occurence relativa al text
		SELECT INTO existing_occ *
		FROM text_occurence AS occ
		WHERE occ.start = tex_start AND 
			occ.end = tex_end;

		-- se la lista dei risultati è vuota
		IF existing_occ IS NULL THEN
	
			-- inserimento dell'occurence
			INSERT INTO text_occurence("start", "end", id_text)
				VALUES(tex_start, tex_end, existing_text.id_text) RETURNING id_text_occurence INTO pk;

			SELECT INTO existing_occ *
			FROM text_occurence AS occ
			WHERE occ.id_text_occurence = pk;
		END IF;		

	END IF;
		
RETURN existing_occ;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_return_text(text, integer, integer, integer) IS 'Funzione che riceve i dati relativi a una porzione di testo, se è gia presente nel db lo restituisce altrimenti lo crea';
-----
CREATE OR REPLACE FUNCTION create_rto_from_pos_type(pstart integer, pend integer, obj_type varchar, is_plu boolean, nw_label text)
  RETURNS textual_objects AS
$BODY$DECLARE
i integer;
obj integer[];
obj_temp record;
nw_obj_type integer;
nw_rto textual_objects;
first_start integer;
plu boolean;
BEGIN
i:=1;
FOR obj_temp IN SELECT objs.* FROM textual_objects AS objs, elements AS el, text_occurence AS occ 	
WHERE objs.start>pstart AND objs.start<pend 
  AND objs.type_name=obj_type AND objs.is_plural=is_plu AND 
  objs.id_tex_obj=el.id_tex_obj AND  	  
  el.id_text_occurence=occ.id_text_occurence AND 	  
  occ.end<pend LOOP

	IF i=1 THEN
		first_start=obj_temp.start;
	END IF;

	obj[i]=obj_temp.id_tex_obj;
	i=i+1;

END LOOP;

--verifica che l'insieme non sia vuoto
IF i=1 THEN
	RAISE EXCEPTION 'Intervallo vuoto, nessun oggetto ritrovato';
END IF;

--verifica esistenza tipologia dell'oggetto


IF NOT EXISTS (SELECT * FROM type_composition WHERE type_contained = obj_type) AND 
EXISTS (SELECT * FROM types WHERE type_name = obj_type AND extends IS NULL) THEN
	plu = false;
ELSE
	plu = true;
END IF;

IF EXISTS (SELECT id_tex_obj 
	FROM textual_objects
	WHERE start=first_start AND type_name=obj_type AND is_plural=is_plu) THEN
		RAISE EXCEPTION 'RTO già esistente';
	ELSE
	nw_rto=create_or_return_rto(obj, obj_type, plu, nw_label);
END IF;

RETURN nw_rto;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_rto_from_pos_type(integer, integer, varchar, boolean, text) IS 'Crea un repeated textual object contenente gli oggetti ricavati con la funzione select_obj_from_pos_type';
-----
CREATE OR REPLACE FUNCTION get_all_attribute_for_obj(obj_id integer)	
  RETURNS SETOF attribute_values AS
$BODY$DECLARE

BEGIN

--recupero degli attributi
RETURN QUERY SELECT *
		FROM attribute_values
		WHERE id_tex_obj=obj_id
		ORDER BY timestamp;


END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_all_attribute_for_obj(integer) IS 'Restituisce tutti gli attributi associtati ad un textual object';
-----
CREATE OR REPLACE FUNCTION get_all_attribute_for_user(att_user integer)
  RETURNS SETOF attribute_values AS
$BODY$DECLARE

BEGIN
--recupero degli attributi
RETURN QUERY SELECT *
	FROM attribute_values
	WHERE id_user=att_user
	ORDER BY timestamp;


END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_all_attribute_for_user(integer) IS 'Restituisce tutti gli attributi creati da un utente';
-----
CREATE OR REPLACE FUNCTION get_all_component(obj_id integer)
  RETURNS integer[] AS
$BODY$DECLARE
	component integer[];
	existing_comp objects_composition;
	i integer;
BEGIN

	--inizializzatione iteratore
	i:=1;

	--ricerca e caricamento sul vettore delle componenti
	FOR existing_comp IN SELECT * FROM objects_composition
	WHERE id_container=obj_id
		ORDER BY pos LOOP
	
		component[i]=existing_comp.id_contained;
		i=i+1;			

	END LOOP;
	
	--verifica che esistano component
	IF component IS NULL THEN
		RAISE EXCEPTION 'Non ci sono componenti per questo oggetto';
	END IF;		
	
RETURN component;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_all_component(integer) IS 'Restituisce tutte le componenti di un oggetto testuale';
-----
CREATE OR REPLACE FUNCTION get_container_obj(tex_obj integer, container_ty integer)
  RETURNS integer[] AS
$BODY$DECLARE
	container_obj_id integer;
	container_obj integer[];
	iter integer;
BEGIN
iter:=1;

--verifica se limitare le ricerca ad un solo tipo
IF container_ty = 0 THEN

	--selezione di tutti i container
	FOR container_obj_id IN SELECT id_container FROM objects_composition 
	WHERE id_contained = tex_obj LOOP

	container_obj[iter]=container_obj_id;
	iter=iter+1;

	END LOOP;

	ELSE
	--ricerca del container di tipo "container_ty"
	FOR container_obj_id IN SELECT obj.id_tex_obj 
	FROM objects_composition AS comp, textual_objects AS obj
	WHERE comp.id_contained = tex_obj AND
		comp.id_container = obj.id_tex_obj AND
		obj.id_type = container_ty LOOP

	container_obj[iter]=container_obj_id;
	iter=iter+1;

	END LOOP;
END IF;

IF container_obj IS NULL THEN
	RAISE EXCEPTION 'Nessun oggetto container trovato';
END IF;		

RETURN container_obj;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_container_obj(integer, integer) IS 'Ricerca tutti gli oggetti che contengono quello fornito in input';
-----
CREATE OR REPLACE FUNCTION get_obj_from_text(testo text, lunghezza integer)
  RETURNS SETOF textual_objects AS
$BODY$DECLARE

BEGIN

RETURN QUERY SELECT obj.*
		FROM texts AS tex, text_occurence AS occ,
		elements AS el, textual_objects AS obj
		WHERE tex.lenght=lunghezza AND tex.cached_text=testo
		AND tex.id_text=occ.id_text
		AND occ.id_text_occurence=el.id_text_occurence
		AND el.id_tex_obj=obj.id_tex_obj;
	

END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_obj_from_text(text, integer) IS 'Restituisce tutti i single textual object associati ad un testo';
-----
CREATE OR REPLACE FUNCTION get_text_from_id(tex_obj integer[])
  RETURNS texts[] AS
$BODY$DECLARE
	existing_tex texts;
	result texts[];
BEGIN

--ciclo che scrorre gli elementi del vettore contenete l'input
FOR i IN array_lower(tex_obj,1) .. array_upper(tex_obj,1) LOOP	

	--ricerca e caricamento sul vettore dei testi
	SELECT INTO existing_tex tex.* 
	FROM elements AS el, text_occurence AS toc, texts AS tex
	WHERE el.id_tex_obj=tex_obj[i] AND 
		el.id_text_occurence=toc.id_text_occurence AND
		toc.id_text=tex.id_text;

	--inserimento del valore nel vettore di output	
	result[i]=existing_tex;
	
END LOOP;

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_text_from_id(integer[]) IS 'Funzione che riceve identificatori di textual object e restituisce il testo relativo ';
-----
CREATE OR REPLACE FUNCTION get_text_from_text(part text, tlenght integer, max integer)
  RETURNS text[] AS
$BODY$DECLARE
testi texts;
ris text[];
i integer;
BEGIN
i:=1;
IF max=0 THEN
FOR testi IN SELECT *
  FROM texts
  WHERE lenght>tlenght AND cached_text LIKE '%'||part||'%' LOOP

	ris[i]=testi.cached_text;
	i=i+1;
	
END LOOP;
ELSE
FOR testi IN SELECT *
  FROM texts
  WHERE lenght>tlenght AND lenght<max AND cached_text LIKE '%'||part||'%' LOOP

	ris[i]=testi.cached_text;
	i=i+1;
	
END LOOP;

END IF;

RETURN ris;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION get_text_from_text(text, integer, integer) IS 'Partendo da una stringa vengono restituiti tutti i testi con lunghezza massima definita dall''utente che la contengono. Se la lunghezza massima è posta a zero vengono restituiti tutti i testi.';
-----
CREATE OR REPLACE FUNCTION insert_att_type(nome_tipo varchar[], etichette varchar[], tipo varchar[], is_plu boolean[], edit boolean[])
  RETURNS integer[] AS
$BODY$DECLARE
	result integer[];
	iter integer;
BEGIN
iter :=1;

FOR i IN array_lower(nome_tipo,1) .. array_upper(nome_tipo,1) LOOP

	--verifica che il tipo non sia stato già inserito
	IF EXISTS (SELECT * FROM attribute_types WHERE attribute_types.type_name=nome_tipo[i] AND attribute_types.label=etichette[i])
	OR (etichette[i] = '') OR (tipo[i] = '')
	THEN
		--se il tipo esiste viene restituito
		result[iter]=i;
		iter=iter+1;
	ELSE
		--inserimento del nuovo tipo
		INSERT INTO attribute_types(type_name, label, type, is_plural, editable)
		VALUES(nome_tipo[i], etichette[i], tipo[i], is_plu[i], edit[i]);
	END IF;
	
END LOOP;	

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION insert_att_type(varchar[], varchar[], varchar[], boolean[], boolean[]) IS 'Funzione che riceve i dati relativi alle tipologie di attributo e se possibile le inserisce';
-----
CREATE OR REPLACE FUNCTION insert_method(type varchar[], etichette varchar[], sintassi text[], is_plu boolean[], codice bytea[])
  RETURNS integer[] AS
$BODY$DECLARE
	result integer[];
	iter integer;
BEGIN
iter :=1;

FOR i IN array_lower(type,1) .. array_upper(type,1) LOOP

	--verifica che il tipo non sia stato già߽ inserito
	IF EXISTS (SELECT * FROM methods WHERE methods.type_name=type[i] AND methods.label=etichette[i])
	OR (etichette[i] = '') OR (sintassi[i] = '')
	THEN
		--se il tipo esiste viene restituito
		result[iter]=i;
		iter=iter+1;
	ELSE
		--inserimento del nuovo tipo
		INSERT INTO methods(type_name, label, syntax, code, is_plural)
		VALUES(type[i], etichette[i], sintassi[i], codice[i], is_plu[i]);
	END IF;
	
END LOOP;	

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION insert_method(varchar[], varchar[], text[], boolean[], bytea[]) IS 'Funzione che riceve i dati relativi ai metodi e se possibile li inserisce';
-----
CREATE OR REPLACE FUNCTION insert_type(nome varchar[], nome_plurale varchar[], description text[], supertype varchar[]) 
  RETURNS integer[] AS
$BODY$DECLARE
	result integer[];
	iter integer;
	existing_type types;
	relty types;
BEGIN
iter :=1;

--creazione dei nuovi tipi
FOR i IN array_lower(nome,1) .. array_upper(nome,1) LOOP

	--verifica che il tipo non sia stato già inserito
	IF EXISTS (SELECT * FROM types WHERE types.type_name=nome[i] OR types.plural_name=nome[i] OR 
				types.type_name=nome_plurale[i] OR types.plural_name=nome_plurale[i]) OR 
				(nome[i] = '') OR (nome_plurale[i] = '')
	THEN
		--se esiste viene restituito
		result[iter]=i;
		iter=iter+1;
	ELSE
		--inserimento del nuovo tipo
		INSERT INTO types(type_name, plural_name, description)
		VALUES(nome[i], nome_plurale[i], description[i]);
	END IF;
			
END LOOP;	

--creazione dei collegamenti tra tipi
FOR i IN array_lower(nome,1) .. array_upper(nome,1) LOOP

	IF supertype[i] IS NOT NULL THEN	
		--controllo se esiste l'estensione
		IF EXISTS (SELECT * FROM types WHERE types.type_name = supertype[i]) THEN
			--caricamento dell'estensione
			UPDATE types SET extends = supertype[i] WHERE types.type_name = nome[i];
		ELSE
			--manca il supertipo -> cancellazione del tipo appena inserito...
			DELETE FROM types WHERE types.type_name = nome[i];
			
			--...e segnalazione dell'errore
			result[iter]=i;
			iter=iter+1;
		END IF;
	END IF;
END LOOP;	

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION insert_type(varchar[], varchar[], text[], varchar[]) IS 'Funzione che riceve vettori contenenti i dati relativi ai tipi e se possibile li inserisce';
-----
CREATE OR REPLACE FUNCTION insert_user(cognome text[], nome text[], alias text[])
  RETURNS integer[] AS
$BODY$DECLARE
	result integer[];
	iter integer;
BEGIN
	iter:=1;
	FOR i IN array_lower(cognome,1) .. array_upper(cognome,1) LOOP
		IF EXISTS (SELECT nickname FROM users
		WHERE nickname=alias[i])THEN
			result[iter]=i;
			iter=iter+1;
		ELSE
			INSERT INTO users(lastname, firstname, nickname)
			VALUES(cognome[i], nome[i], alias[i]);
		END IF;
	END LOOP;	

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION insert_user(text[], text[], text[]) IS 'Funzione che riceve un insieme di utenti e se possibile li inserisce';
-----
CREATE OR REPLACE FUNCTION select_obj_from_pos_type(pstart integer, pend integer, obj_type varchar, is_plu boolean)
  RETURNS SETOF textual_objects AS
$BODY$DECLARE

BEGIN

RETURN QUERY 
	SELECT objs.* 
	FROM textual_objects AS objs, elements AS el, text_occurence AS occ 	
	WHERE objs.start>pstart AND objs.start<pend 
	AND objs.type_name=obj_type AND objs.is_plural=is_plu AND 
	objs.id_tex_obj=el.id_tex_obj AND
	el.id_text_occurence=occ.id_text_occurence AND 	  
	occ.end<pend;
	
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION select_obj_from_pos_type(integer, integer, varchar, boolean) IS 'Restituisce tutti gli oggetti di un determinato tipo contenuti in un intervallo';
-----
CREATE OR REPLACE FUNCTION create_or_update_schema(nome varchar, descrizione text)
  RETURNS schema AS
$BODY$DECLARE
	max varchar;
	min varchar;
	result schema;
	candidate RECORD;
BEGIN
	IF (nome = '') OR (nome IS NULL) THEN
		RAISE EXCEPTION 'var nome is empty';
	END IF;
	
	--search among all types which are not extended
	FOR candidate IN SELECT * FROM types WHERE extends IS NULL LOOP
	
		--search for the max_unit
		IF NOT EXISTS (SELECT * FROM type_composition WHERE type_composition.contained_type = candidate.type_name) THEN
			IF max IS NULL THEN
				max := candidate.type_name;
			ELSE
				RAISE EXCEPTION 'Bad Manuzio schema: more then one max_unit has been found.';
			END IF;
		ELSE
			RAISE EXCEPTION 'Bad Manuzio schema: no max_unit has been found.';
		END IF;
		
		--search for the min_unit
		IF NOT EXISTS (SELECT * FROM type_composition WHERE type_composition.container_type = candidate.type_name) THEN
			IF min IS NULL THEN
				min := candidate.type_name;
			ELSE
				RAISE EXCEPTION 'Bad Manuzio schema: more then one min_unit has been found.';
			END IF;
		ELSE
			RAISE EXCEPTION 'Bad Manuzio schema: no min_unit has been found.';
		END IF;
	END LOOP;
	
	IF candidate IS NULL THEN
		RAISE EXCEPTION 'Bad Manuzio schema: no suitable types for max_unit or min_unit found.';
	END IF;
	
	--checks if there is already a line inserted and deletes it if found
	IF EXISTS (SELECT * FROM schema) THEN
		DELETE FROM schema;
	END IF;
	
	--updates the schema
	INSERT INTO schema (schema_name, max_unit, min_unit, description, db_version, timestamp)
			VALUES(nome, max, min, descrizione, 3.1, now());
			
	--returns the updated schema
	SELECT INTO result * FROM schema;

RETURN result;
END;$BODY$
  LANGUAGE plpgsql VOLATILE;
-----
COMMENT ON FUNCTION create_or_update_schema(varchar, text) IS 'Crea uno schema o ne aggiorna uno giw esistente.';
----