import { defineStore } from 'pinia'
import { useAuthStore } from './auth'

export const useApiStore = defineStore('api', {
	actions: {
		baseUrl () {
			const config = useRuntimeConfig()
			return config.public.apiBase || ''
		},

		authHeaders () {
			const auth = useAuthStore()
			return auth.token
				? { Authorization: `Bearer ${auth.token}` }
				: {}
		},

		async request (path, options = {}) {
			const url = `${this.baseUrl()}${path}`

			const mergedHeaders = {
				...this.authHeaders(),
				...(options.headers || {})
			}

			return await $fetch(url, {
				...options,
				headers: mergedHeaders
			})
		},

		get (path, options = {}) {
			return this.request(path, { ...options, method: 'GET' })
		},

		post (path, body, options = {}) {
			return this.request(path, { ...options, method: 'POST', body })
		},

		put (path, body, options = {}) {
			return this.request(path, { ...options, method: 'PUT', body })
		},

		patch (path, body, options = {}) {
			return this.request(path, { ...options, method: 'PATCH', body })
		},

		del (path, options = {}) {
			return this.request(path, { ...options, method: 'DELETE' })
		},

		//users
		getUsers (params) {
			return this.get('/users', { params })
		},
		getUser (userId) {
			return this.get(`/users/${userId}`)
		},
		createUser (payload) {
			return this.post('/users', payload)
		},
		updateUser (userId, payload) {
			return this.put(`/users/${userId}`, payload)
		},
		updateUserActive (userId, payload) {
			return this.put(`/users/${userId}/active`, payload)
		},
		updateUserRole (userId, payload) {
			return this.put(`/users/${userId}/role`, payload)
		},
		deleteUser (userId) {
			return this.del(`/users/${userId}`)
		},
		subscribeUserTag (tagId) {
			return this.post('/users/me/tags', { tag: tagId })
		},
		unsubscribeUserTag (tagId) {
			return this.del(`/users/me/tags/${tagId}`)
		},
		updateUserEmail (payload) {
			return this.patch(`/users/me/email`, payload)
		},
		updateUserPassword (payload) {
			return this.patch(`/users/me/password`, payload)
		},

		//publicações
		getPublicacoes (params) {
			return this.get('/publications', { params })
		},
		getPublicacoesVisiveis(){	
			return this.get('/publications/visible')
		},
		getPublicacoesOcultas(){
			return this.get('/publications/hidden')
		},
		getMinhasPublicacoes () {
			return this.get('/publications/me')
		},
		getPublicacoesRecomendadas () {
			return this.get('/publications/recommended')
		},
		getPublicacao (id) {
			return this.get(`/publications/${id}`)
		},
		createPublicacao (payload) {
			return this.post('/publications', payload)
		},
		updatePublicacao (id, payload) {
			return this.put(`/publications/${id}`, payload)
		},
		updatePublicacaoVisibility (id, payload) {
			return this.put(`/publications/${id}/visibility`, payload)
		},
		deletePublicacao (id) {
			return this.del(`/publications/${id}`)
		},
		addPublicacaoRating (id, payload) {
			return this.post(`/publications/${id}/ratings`, payload)
		},
		updatePublicacaoRating (id, ratingId, payload) {
			return this.put(`/publications/${id}/ratings/${ratingId}`, payload)
		},
		deletePublicacaoRating (id, ratingId) {
			return this.del(`/publications/${id}/ratings/${ratingId}`)
		},
		addPublicacaoTag (id, payload) {
			return this.post(`/publications/${id}/tag`, payload)
		},
		deletePublicacaoTag (id, tagId) {
			return this.del(`/publications/${id}/tag/${tagId}`)
		},
		addPublicacaoComment (id, payload) {
			return this.post(`/publications/${id}/comments`, payload)
		},
		updatePublicacaoComment (id, commentId, payload) {
			return this.put(`/publications/${id}/comments/${commentId}`, payload)
		},
		getPublicacaoComments (id) {
			return this.get(`/publications/${id}/comments`)
		},
		getPublicacaoHiddenComments (id) {
			return this.get(`/publications/${id}/comments/hidden`)
		},
		updatePublicacaoCommentVisibility (id, commentId, payload) {
			return this.put(`/publications/${id}/comments/${commentId}/visibility`, payload)
		},

		//tags
		createTag (payload) {
			return this.post('/tags', payload)
		},
		deleteTag (tagId) {
			return this.del(`/tags/${tagId}`)
		},
		updateTag (tagId, payload) {
			return this.put(`/tags/${tagId}`, payload)
		},
		updateTagVisibility (tagId, payload) {
			return this.put(`/tags/${tagId}/visibility`, payload)
		},
		getTags () {
			return this.get('/tags')
		},
		getVisibleTags () {
			return this.get('/tags/visible')
		},

		//áreas
		createArea (payload) {
			return this.post('/areas', payload)
		},
		deleteArea (areaId) {
			return this.del(`/areas/${areaId}`)
		},
		getAreas () {
			return this.get('/areas')
		},

		//roles
		getRoles () {
			return this.get('/roles')
		},

		//histórico
		getMyHistory () {
			return this.get('/history/me')
		},
		getUserHistory (userId) {
			return this.get(`/history/${userId}`)
		},

		//auth (via API store)
		login (credentials) {
			return this.post('/auth/login', credentials)
		},
		logoutServer () {
			return this.post('/auth/logout')
		},
		resetPassword (payload) {
			return this.post('/auth/reset', payload)
		},

		//ia
		gerarResumoIA (payload) {
			return this.post('/ia/summary', payload)
		},

		//estatísticas
		getEstatisticasPublicacoesPorArea () {
			return this.get('/statistics/publications-by-area')
		},
		getEstatisticasMediaRatingPorArea () {
			return this.get('/statistics/average-rating-by-area')
		},
		getEstatisticasMediaRatingPorTag () {
			return this.get('/statistics/average-rating-by-tag')
		},
		getEstatisticasEvolucaoSubmissoes () {
			return this.get('/statistics/evolution-submissions')
		},

		//notificações
		getNotifications () {
			return this.get('/notifications')
		},

	}
})

