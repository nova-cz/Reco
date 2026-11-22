-- Reseñas visibles en el perfil de un vendedor. La app no puede leer los pedidos de otros, así que esta vista
-- (security definer) expone solo lo necesario: quién calificó, a quién y qué equipo (sin montos ni puntos de entrega).
create view resenas_publicas with (security_invoker = false) as
select c.id, c.pedido_id, c.estrellas, c.etiquetas, c.comentario, c.creado,
       p.vendedor_id, p.comprador_id, m.modelo as producto
from calificaciones c
join pedidos p on p.id = c.pedido_id
join productos pr on pr.id = p.producto_id
join modelos m on m.id = pr.modelo_id;
revoke all on resenas_publicas from anon, public;
grant select on resenas_publicas to authenticated;
