/*
 * Copyright 2011, Mysema Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mysema.query.sql;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.query.JoinFlag;
import com.mysema.query.Query;
import com.mysema.query.QueryFlag;
import com.mysema.query.QueryFlag.Position;
import com.mysema.query.QueryMetadata;
import com.mysema.query.SearchResults;
import com.mysema.query.Tuple;
import com.mysema.query.support.Expressions;
import com.mysema.query.support.ProjectableQuery;
import com.mysema.query.support.QueryMixin;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.ExpressionUtils;
import com.mysema.query.types.OperationImpl;
import com.mysema.query.types.ParamExpression;
import com.mysema.query.types.ParamNotSetException;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.SubQueryExpression;
import com.mysema.query.types.expr.Wildcard;
import com.mysema.query.types.query.ListSubQuery;
import com.mysema.query.types.template.NumberTemplate;
import com.mysema.query.types.template.SimpleTemplate;

/**
 * ProjectableSQLQuery is the base type for SQL query implementations
 *
 * @param <Q> concrete subtype
 */
public abstract class ProjectableSQLQuery<Q extends ProjectableSQLQuery<Q> & Query<Q>> extends ProjectableQuery<Q> implements SQLCommonQuery<Q> {

    protected final Configuration configuration;
    
    @Nullable
    protected Expression<?> union;

    protected boolean unionAll;

    @SuppressWarnings("unchecked")
    public ProjectableSQLQuery(QueryMixin<Q> queryMixin, Configuration configuration) {
        super(queryMixin);
        this.queryMixin.setSelf((Q) this);
        this.configuration = configuration;
    }

    /**
     * Add the given String literal as a join flag to the last added join with the position
     * BEFORE_TARGET
     *
     * @param flag
     * @return
     */
    @Override
    public Q addJoinFlag(String flag) {
        return addJoinFlag(flag, JoinFlag.Position.BEFORE_TARGET);
    }

    /**
     * Add the given String literal as a join flag to the last added join
     *
     * @param flag
     * @param position
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Q addJoinFlag(String flag, JoinFlag.Position position) {
        queryMixin.addJoinFlag(new JoinFlag(flag, position));
        return (Q)this;
    }

    /**
     * Add the given prefix and expression as a general query flag
     *
     * @param position position of the flag
     * @param prefix prefix for the flag
     * @param expr expression of the flag
     * @return
     */
    @Override
    public Q addFlag(Position position, String prefix, Expression<?> expr) {
        Expression<?> flag = SimpleTemplate.create(expr.getType(), prefix + "{0}", expr);
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    /**
     * Add the given query flag
     *
     * @param flag
     * @return
     */
    public Q addFlag(QueryFlag flag) {
        return queryMixin.addFlag(flag);
    }

    /**
     * Add the given String literal as query flag
     *
     * @param position
     * @param flag
     * @return
     */
    @Override
    public Q addFlag(Position position, String flag) {
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    /**
     * Add the given Expression as a query flag
     *
     * @param position
     * @param flag
     * @return
     */
    @Override
    public Q addFlag(Position position, Expression<?> flag) {
        return queryMixin.addFlag(new QueryFlag(position, flag));
    }

    @Override
    public long count() {
        Number number = uniqueResult(Wildcard.countAsInt);
        return number.longValue();
    }

    @Override
    public boolean exists() {
        return limit(1).singleResult(NumberTemplate.ONE) != null;
    }

    public Q from(Expression<?> arg) {
        return queryMixin.from(arg);
    }

    @Override
    public Q from(Expression<?>... args) {
        return queryMixin.from(args);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Q from(SubQueryExpression<?> subQuery, Path<?> alias) {
        return queryMixin.from(ExpressionUtils.as((Expression) subQuery, alias));
    }

    @Override
    public Q fullJoin(EntityPath<?> target) {
        return queryMixin.fullJoin(target);
    }

    @Override
    public <E> Q fullJoin(RelationalFunctionCall<E> target, Path<E> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public Q fullJoin(SubQueryExpression<?> target, Path<?> alias) {
        return queryMixin.fullJoin(target, alias);
    }

    @Override
    public <E> Q fullJoin(ForeignKey<E> key, RelationalPath<E> entity) {
        return queryMixin.fullJoin(entity).on(key.on(entity));
    }

    @Override
    public Q innerJoin(EntityPath<?> target) {
        return queryMixin.innerJoin(target);
    }

    @Override
    public <E> Q innerJoin(RelationalFunctionCall<E> target, Path<E> alias) {
        return queryMixin.innerJoin(target, alias);
    }

    @Override
    public Q innerJoin(SubQueryExpression<?> target, Path<?> alias) {
        return queryMixin.innerJoin(target, alias);
    }

    @Override
    public <E> Q innerJoin(ForeignKey<E> key, RelationalPath<E> entity) {
        return queryMixin.innerJoin(entity).on(key.on(entity));
    }

    @Override
    public Q join(EntityPath<?> target) {
        return queryMixin.join(target);
    }

    @Override
    public <E> Q join(RelationalFunctionCall<E> target, Path<E> alias) {
        return queryMixin.join(target, alias);
    }

    @Override
    public Q join(SubQueryExpression<?> target, Path<?> alias) {
        return queryMixin.join(target, alias);
    }

    @Override
    public <E> Q join(ForeignKey<E> key, RelationalPath<E>  entity) {
        return queryMixin.join(entity).on(key.on(entity));
    }

    @Override
    public Q leftJoin(EntityPath<?> target) {
        return queryMixin.leftJoin(target);
    }

    @Override
    public <E> Q leftJoin(RelationalFunctionCall<E> target, Path<E> alias) {
        return queryMixin.leftJoin(target, alias);
    }

    @Override
    public Q leftJoin(SubQueryExpression<?> target, Path<?> alias) {
        return queryMixin.leftJoin(target, alias);
    }

    @Override
    public <E> Q leftJoin(ForeignKey<E> key, RelationalPath<E>  entity) {
        return queryMixin.leftJoin(entity).on(key.on(entity));
    }

    @Override
    public Q rightJoin(EntityPath<?> target) {
        return queryMixin.rightJoin(target);
    }

    @Override
    public <E> Q rightJoin(RelationalFunctionCall<E> target, Path<E> alias) {
        return queryMixin.rightJoin(target, alias);
    }

    @Override
    public Q rightJoin(SubQueryExpression<?> target, Path<?> alias) {
        return queryMixin.rightJoin(target, alias);
    }

    @Override
    public <E> Q rightJoin(ForeignKey<E> key, RelationalPath<E>  entity) {
        return queryMixin.rightJoin(entity).on(key.on(entity));
    }

    public QueryMetadata getMetadata() {
        return queryMixin.getMetadata();
    }

    @SuppressWarnings("unchecked")
    private <RT> Union<RT> innerUnion(SubQueryExpression<?>... sq) {
        queryMixin.getMetadata().setValidate(false);
        if (!queryMixin.getMetadata().getJoins().isEmpty()) {
            throw new IllegalArgumentException("Don't mix union and from");
        }
        this.union = UnionUtils.union(sq, unionAll);
        return new UnionImpl<Q ,RT>((Q)this, sq[0].getMetadata().getProjection());
    }

    @Override
    public CloseableIterator<Tuple> iterate(Expression<?>... args) {
        return iterate(queryMixin.createProjection(args));
    }

    @Override
    public List<Tuple> list(Expression<?>... args) {
        return list(queryMixin.createProjection(args));
    }

    @Override
    public SearchResults<Tuple> listResults(Expression<?>... args) {
        return listResults(queryMixin.createProjection(args));
    }

    public Q on(Predicate condition) {
        return queryMixin.on(condition);
    }

    @Override
    public Q on(Predicate... conditions) {
        return queryMixin.on(conditions);
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Union<RT> union(ListSubQuery<RT>... sq) {
        return innerUnion(sq);
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Q union(Path<?> alias, ListSubQuery<RT>... sq) {
        return from(UnionUtils.union(sq, alias, false));
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Union<RT> union(SubQueryExpression<RT>... sq) {
        return innerUnion(sq);
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Q union(Path<?> alias, SubQueryExpression<RT>... sq) {
        return from(UnionUtils.union(sq, alias, false));
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Union<RT> unionAll(ListSubQuery<RT>... sq) {
        unionAll = true;
        return innerUnion(sq);
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Q unionAll(Path<?> alias, ListSubQuery<RT>... sq) {
        return from(UnionUtils.union(sq, alias, true));
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Union<RT> unionAll(SubQueryExpression<RT>... sq) {
        unionAll = true;
        return innerUnion(sq);
    }

    /**
     * Creates an union expression for the given subqueries
     *
     * @param <RT>
     * @param sq
     * @return
     */
    public <RT> Q unionAll(Path<?> alias, SubQueryExpression<RT>... sq) {
        return from(UnionUtils.union(sq, alias, true));
    }

    @Override
    public Tuple uniqueResult(Expression<?>... args) {
        return uniqueResult(queryMixin.createProjection(args));
    }

    @Override
    public <RT> RT uniqueResult(Expression<RT> expr) {
        if (getMetadata().getModifiers().getLimit() == null
           && !expr.toString().contains("count(")) {
            limit(2);
        }
        CloseableIterator<RT> iterator = iterate(expr);
        return uniqueResult(iterator);
    }

    @Override
    public Q withRecursive(Path<?> alias, SubQueryExpression<?> query) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, query);
    }

    @Override
    public Q withRecursive(Path<?> alias, Expression<?> query) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, query);
    }

    @Override
    public WithBuilder<Q> withRecursive(Path<?> alias, Path<?>... columns) {
        queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, SQLTemplates.RECURSIVE));
        return with(alias, columns);
    }

    @Override
    public Q with(Path<?> alias, SubQueryExpression<?> query) {
        Expression<?> expr = OperationImpl.create(alias.getType(), SQLOps.WITH_ALIAS, alias, query);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public Q with(Path<?> alias, Expression<?> query) {
        Expression<?> expr = OperationImpl.create(alias.getType(), SQLOps.WITH_ALIAS, alias, query);
        return queryMixin.addFlag(new QueryFlag(QueryFlag.Position.WITH, expr));
    }

    @Override
    public WithBuilder<Q> with(Path<?> alias, Path<?>... columns) {
        Expression<?> columnsCombined = ExpressionUtils.list(Object.class, columns);
        Expression<?> aliasCombined = Expressions.operation(alias.getType(), SQLOps.WITH_COLUMNS, alias, columnsCombined);
        return new WithBuilder<Q>(queryMixin, aliasCombined);
    }
    
    protected void clone(Q query) {
        this.union = query.union;
        this.unionAll = query.unionAll;
    }

    @Override
    public abstract Q clone();
    
    protected abstract SQLSerializer createSerializer();
    
    protected SQLSerializer serialize(boolean forCountRow) {
        SQLSerializer serializer = createSerializer();
        if (union != null) {
            serializer.serializeUnion(union, queryMixin.getMetadata(), unionAll);
        } else {
            serializer.serialize(queryMixin.getMetadata(), forCountRow);
        }
        return serializer;
    }
    
    /**
     * Get the query as an SQL query string and bindings
     *
     * @param exprs
     * @return
     */
    public SQLBindings getSQL(Expression<?>... exprs) {
        queryMixin.addProjection(exprs);
        SQLSerializer serializer = serialize(false);
        ImmutableList.Builder<Object> args = ImmutableList.builder();
        Map<ParamExpression<?>, Object> params = getMetadata().getParams();
        for (Object o : serializer.getConstants()) {
            if (o instanceof ParamExpression) {
                if (!params.containsKey(o)) {
                    throw new ParamNotSetException((ParamExpression<?>) o);
                }
                o = queryMixin.getMetadata().getParams().get(o);
            }
            args.add(o);
        }
        return new SQLBindings(serializer.toString(), args.build());
    }
    
    public String toString() {
        SQLSerializer serializer = serialize(false);
        return serializer.toString().trim();
    }

}
